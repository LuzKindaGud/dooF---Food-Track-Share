package com.example.doancoso3.ui.fooditem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doancoso3.data.model.FoodItemEntity
import com.example.doancoso3.data.model.StorageLocation
import com.example.doancoso3.data.repository.AuthRepository
import com.example.doancoso3.data.repository.FoodItemRepository
import com.example.doancoso3.utils.FoodItemValidator
import com.example.doancoso3.utils.ValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * Validation errors shown inline on the add/edit form.
 */
data class FoodItemFormErrors(
    val nameError: String? = null,
    val quantityError: String? = null,
    val expiryError: String? = null
) {
    val hasError: Boolean
        get() = nameError != null || quantityError != null || expiryError != null
}

/**
 * One-shot UI events emitted by the ViewModel for the Inventory screen.
 */
sealed interface InventoryUiEvent {
    data object ItemSaved : InventoryUiEvent
    data object ItemDeleted : InventoryUiEvent
    data class Message(val text: String) : InventoryUiEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FoodItemViewModel @Inject constructor(
    private val foodItemRepository: FoodItemRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedLocation = MutableStateFlow<StorageLocation?>(null)
    val selectedLocation: StateFlow<StorageLocation?> = _selectedLocation

    /** When true, only items expiring within [EXPIRING_SOON_DAYS] days (or already expired) are shown. */
    private val _expiringSoonOnly = MutableStateFlow(false)
    val expiringSoonOnly: StateFlow<Boolean> = _expiringSoonOnly

    private val _formErrors = MutableStateFlow(FoodItemFormErrors())
    val formErrors: StateFlow<FoodItemFormErrors> = _formErrors

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _events = Channel<InventoryUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val currentUserId: String? get() = authRepository.getCurrentUser()?.uid

    private val userProfile = currentUserId?.let { uid ->
        authRepository.observeUserProfile(uid)
    } ?: flowOf(null)

    /** The family group the current user belongs to (null when not in a group). */
    val currentFamilyId: StateFlow<String?> = userProfile
        .map { it?.familyId }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val rawInventory: StateFlow<List<FoodItemEntity>> = userProfile
        .flatMapLatest { user ->
            val familyId = user?.familyId
            if (familyId.isNullOrEmpty()) {
                flowOf(emptyList())
            } else {
                foodItemRepository.getItems(familyId)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Inventory after applying search, location and expiring-soon filters reactively. */
    val inventoryItems: StateFlow<List<FoodItemEntity>> = combine(
        rawInventory,
        _searchQuery,
        _selectedLocation,
        _expiringSoonOnly
    ) { items, query, location, expiringSoon ->
        items.filter { item ->
            val matchesQuery = query.isBlank() || item.name.contains(query, ignoreCase = true)
            val matchesLocation = location == null || item.storageLocation == location.name
            val matchesExpiring = !expiringSoon || isExpiringSoon(item.expiryDate)
            matchesQuery && matchesLocation && matchesExpiring
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groupedItems: StateFlow<Map<StorageLocation, List<FoodItemEntity>>> = inventoryItems
        .map { items ->
            items.groupBy {
                try {
                    StorageLocation.valueOf(it.storageLocation)
                } catch (e: Exception) {
                    StorageLocation.PANTRY
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedLocation(location: StorageLocation?) {
        _selectedLocation.value = location
        if (location != null) _expiringSoonOnly.value = false
    }

    fun toggleExpiringSoon(enabled: Boolean) {
        _expiringSoonOnly.value = enabled
        if (enabled) _selectedLocation.value = null
    }

    fun clearFilters() {
        _selectedLocation.value = null
        _expiringSoonOnly.value = false
    }

    fun clearFormErrors() {
        _formErrors.value = FoodItemFormErrors()
    }

    /**
     * Validates and persists a food item. Pass [existingItem] to edit, or null to create.
     * Writes through the repository which saves to Room first then syncs to Firestore (MVVM offline-first).
     */
    fun saveFoodItem(
        existingItem: FoodItemEntity?,
        name: String,
        quantityText: String,
        expiryDate: Long?,
        location: StorageLocation,
        barcode: String?,
        unit: String = "pcs"
    ) {
        val quantity = quantityText.trim().toDoubleOrNull()

        val nameResult = FoodItemValidator.validateName(name.trim())
        val quantityResult = if (quantity == null) {
            ValidationResult.Error("Số lượng không hợp lệ")
        } else {
            FoodItemValidator.validateQuantity(quantity)
        }
        val expiryResult = FoodItemValidator.validateExpiryDate(expiryDate)

        val errors = FoodItemFormErrors(
            nameError = (nameResult as? ValidationResult.Error)?.message,
            quantityError = (quantityResult as? ValidationResult.Error)?.message,
            expiryError = (expiryResult as? ValidationResult.Error)?.message
        )
        _formErrors.value = errors
        if (errors.hasError || quantity == null || expiryDate == null) return

        viewModelScope.launch {
            _isSaving.value = true
            try {
                val familyId = resolveFamilyId()
                if (familyId.isNullOrEmpty()) {
                    _events.send(InventoryUiEvent.Message("Bạn cần tham gia một nhóm gia đình trước"))
                    return@launch
                }

                val userId = currentUserId ?: existingItem?.createdBy ?: ""
                val now = System.currentTimeMillis()

                if (existingItem == null) {
                    val newItem = FoodItemEntity(
                        id = UUID.randomUUID().toString(),
                        familyId = familyId,
                        name = name.trim(),
                        quantity = quantity,
                        unit = unit,
                        expiryDate = expiryDate,
                        storageLocation = location.name,
                        barcode = barcode?.trim()?.takeIf { it.isNotEmpty() },
                        imageUri = null,
                        createdBy = userId,
                        createdAt = now,
                        updatedAt = now,
                        synced = false,
                        deleted = false
                    )
                    foodItemRepository.addItem(newItem)
                } else {
                    val updated = existingItem.copy(
                        name = name.trim(),
                        quantity = quantity,
                        unit = unit,
                        expiryDate = expiryDate,
                        storageLocation = location.name,
                        barcode = barcode?.trim()?.takeIf { it.isNotEmpty() },
                        updatedAt = now,
                        synced = false
                    )
                    foodItemRepository.updateItem(updated)
                }
                _formErrors.value = FoodItemFormErrors()
                _events.send(InventoryUiEvent.ItemSaved)
            } catch (e: Exception) {
                _events.send(InventoryUiEvent.Message("Lưu thất bại: ${e.localizedMessage ?: "lỗi không xác định"}"))
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            try {
                foodItemRepository.deleteItem(itemId)
                _events.send(InventoryUiEvent.ItemDeleted)
            } catch (e: Exception) {
                _events.send(InventoryUiEvent.Message("Xóa thất bại: ${e.localizedMessage ?: "lỗi không xác định"}"))
            }
        }
    }

    private fun isExpiringSoon(expiryDate: Long): Boolean {
        val diff = expiryDate - System.currentTimeMillis()
        return diff <= EXPIRING_SOON_DAYS * DAY_MILLIS
    }

    /**
     * Resolves the current user's familyId reliably at action time. Prefers the cached
     * StateFlow value, falls back to the local profile, and finally pulls the latest
     * profile from Firestore (covers members who were just approved into a group on
     * another device and whose local cache is stale).
     */
    private suspend fun resolveFamilyId(): String? {
        currentFamilyId.value?.takeIf { it.isNotEmpty() }?.let { return it }

        val uid = currentUserId ?: return null

        authRepository.observeUserProfile(uid).first()?.familyId
            ?.takeIf { it.isNotEmpty() }
            ?.let { return it }

        // Local cache is stale (or empty) — pull the freshest profile from Firestore.
        authRepository.syncUserProfile(uid)
        return authRepository.observeUserProfile(uid).first()?.familyId?.takeIf { it.isNotEmpty() }
    }

    companion object {
        private const val DAY_MILLIS = 24 * 60 * 60 * 1000L
        private const val EXPIRING_SOON_DAYS = 2
    }
}
