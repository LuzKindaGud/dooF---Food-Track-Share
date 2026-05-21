package com.example.doancoso3.ui.fooditem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doancoso3.data.model.FoodItemEntity
import com.example.doancoso3.data.model.StorageLocation
import com.example.doancoso3.data.repository.AuthRepository
import com.example.doancoso3.data.repository.FoodItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    private val userProfile = authRepository.getCurrentUser()?.uid?.let { uid ->
        authRepository.observeUserProfile(uid)
    } ?: flowOf(null)

    val inventoryItems: StateFlow<List<FoodItemEntity>> = userProfile
        .flatMapLatest { user ->
            val familyId = user?.familyId ?: ""
            combine(_searchQuery, _selectedLocation) { query, location ->
                Triple(familyId, query, location)
            }
        }
        .flatMapLatest { (familyId, query, location) ->
            if (familyId.isEmpty()) {
                flowOf(emptyList())
            } else {
                when {
                    query.isNotEmpty() -> foodItemRepository.searchItems(familyId, query)
                    location != null -> foodItemRepository.getItemsByLocation(familyId, location)
                    else -> foodItemRepository.getItems(familyId)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groupedItems: StateFlow<Map<StorageLocation, List<FoodItemEntity>>> = inventoryItems
        .map { items ->
            items.groupBy { 
                try {
                    StorageLocation.valueOf(it.storageLocation)
                } catch (e: Exception) {
                    StorageLocation.PANTRY // Default
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedLocation(location: StorageLocation?) {
        _selectedLocation.value = location
    }

    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            foodItemRepository.deleteItem(itemId)
        }
    }
    
    fun addItem(item: FoodItemEntity) {
        viewModelScope.launch {
            foodItemRepository.addItem(item)
        }
    }

    fun updateItem(item: FoodItemEntity) {
        viewModelScope.launch {
            foodItemRepository.updateItem(item)
        }
    }
}
