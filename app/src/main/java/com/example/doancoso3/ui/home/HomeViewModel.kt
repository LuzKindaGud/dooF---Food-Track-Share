package com.example.doancoso3.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doancoso3.data.local.dao.FoodItemDao
import com.example.doancoso3.data.local.dao.HistoryEntryDao
import com.example.doancoso3.data.local.dao.UserDao
import com.example.doancoso3.data.model.FoodItemEntity
import com.example.doancoso3.data.model.HistoryEntryEntity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val foodItemDao: FoodItemDao,
    private val historyEntryDao: HistoryEntryDao,
    private val userDao: UserDao,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    // --- Dashboard Stats ---
    private val _totalItems = MutableLiveData(0)
    val totalItems: LiveData<Int> = _totalItems

    private val _expiringCount = MutableLiveData(0)
    val expiringCount: LiveData<Int> = _expiringCount

    private val _wasteSaved = MutableLiveData("0kg")
    val wasteSaved: LiveData<String> = _wasteSaved

    private val _savings = MutableLiveData("$0.00")
    val savings: LiveData<String> = _savings

    // --- Recent Inventory ---
    private val _recentItems = MutableLiveData<List<RecentInventoryItem>>(emptyList())
    val recentItems: LiveData<List<RecentInventoryItem>> = _recentItems

    // --- Family Activity ---
    private val _familyActivities = MutableLiveData<List<FamilyActivityItem>>(emptyList())
    val familyActivities: LiveData<List<FamilyActivityItem>> = _familyActivities

    // --- Meal Prep Suggestion ---
    private val _mealPrepCount = MutableLiveData(0)
    val mealPrepCount: LiveData<Int> = _mealPrepCount

    // --- User display name ---
    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            val userId = firebaseAuth.currentUser?.uid ?: return@launch
            val user = userDao.getUserById(userId)
            _userName.postValue(user?.displayName ?: firebaseAuth.currentUser?.displayName ?: "User")

            val familyId = user?.familyId
            if (familyId != null) {
                observeInventory(familyId)
                observeFamilyActivity(familyId)
            }
        }
    }

    private fun observeInventory(familyId: String) {
        viewModelScope.launch {
            foodItemDao.getItemsByFamilyId(familyId).collectLatest { items ->
                _totalItems.postValue(items.size)

                // Count items expiring within 48 hours
                val now = System.currentTimeMillis()
                val in48h = now + (48 * 60 * 60 * 1000)
                val expiring = items.count { it.expiryDate in (now + 1)..in48h }
                _expiringCount.postValue(expiring)

                // Calculate stats (placeholder logic based on consumed items)
                _wasteSaved.postValue("${String.format("%.1f", items.size * 0.087)}kg")
                _savings.postValue("$${String.format("%.2f", items.size * 0.59)}")

                // Recent items
                val recentList = items
                    .sortedByDescending { it.createdAt }
                    .take(5)
                    .map { entity -> mapToRecentItem(entity) }
                _recentItems.postValue(recentList)

                // Meal prep count (expiring within 5 days)
                val in5Days = now + (5 * 24 * 60 * 60 * 1000)
                val expiringSoon = items.count { it.expiryDate in (now + 1)..in5Days }
                _mealPrepCount.postValue(expiringSoon)
            }
        }
    }

    private fun observeFamilyActivity(familyId: String) {
        viewModelScope.launch {
            historyEntryDao.getHistoryByFamilyId(familyId, limit = 5, offset = 0)
                .collectLatest { entries ->
                    val activities = entries.map { entry ->
                        val timeAgo = getTimeAgo(entry.timestamp)
                        val target = entry.foodItemName ?: entry.targetName ?: ""
                        
                        val description = when (entry.actionType) {
                            "ADDED" -> "${entry.userName} added $target"
                            "EDITED" -> "${entry.userName} updated $target"
                            "DELETED" -> "${entry.userName} removed $target"
                            "JOINED" -> "${entry.userName} joined the group"
                            "LEFT" -> "${entry.userName} left the group"
                            "REMOVED" -> "${entry.userName} was removed by admin"
                            else -> "${entry.userName} performed ${entry.actionType.lowercase()} on $target"
                        }

                        FamilyActivityItem(
                            title = description,
                            subtitle = "${entry.actionType} • $timeAgo",
                            isRecent = entries.indexOf(entry) == 0
                        )
                    }
                    if (activities.isEmpty()) {
                        _familyActivities.postValue(
                            listOf(
                                FamilyActivityItem(
                                    "No recent activity",
                                    "Your family's updates will appear here",
                                    true
                                )
                            )
                        )
                    } else {
                        _familyActivities.postValue(activities)
                    }
                }
        }
    }

    private fun mapToRecentItem(entity: FoodItemEntity): RecentInventoryItem {
        val now = System.currentTimeMillis()
        val daysUntilExpiry = ((entity.expiryDate - now) / (1000 * 60 * 60 * 24)).toInt()
        val expiryText = when {
            daysUntilExpiry < 0 -> "Expired"
            daysUntilExpiry == 0 -> "Expires today"
            daysUntilExpiry == 1 -> "Expires tomorrow"
            else -> "Expires in $daysUntilExpiry days"
        }
        val status = when {
            daysUntilExpiry < 0 -> ExpiryDisplayStatus.EXPIRED
            daysUntilExpiry <= 2 -> ExpiryDisplayStatus.WARNING
            else -> ExpiryDisplayStatus.GOOD
        }
        val quantityText = if (entity.quantity > 0) {
            "${entity.quantity.toInt()} left"
        } else {
            "Empty"
        }
        return RecentInventoryItem(
            id = entity.id,
            name = entity.name,
            expiryText = expiryText,
            quantity = quantityText,
            status = status
        )
    }

    private fun getTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val minutes = diff / (1000 * 60)
        val hours = diff / (1000 * 60 * 60)
        val days = diff / (1000 * 60 * 60 * 24)

        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "$minutes mins ago"
            hours < 24 -> "$hours hour${if (hours > 1) "s" else ""} ago"
            days < 7 -> "$days day${if (days > 1) "s" else ""} ago"
            else -> "${days / 7} week${if (days / 7 > 1) "s" else ""} ago"
        }
    }
}

data class RecentInventoryItem(
    val id: String,
    val name: String,
    val expiryText: String,
    val quantity: String,
    val status: ExpiryDisplayStatus
)

enum class ExpiryDisplayStatus {
    GOOD,
    WARNING,
    EXPIRED
}

data class FamilyActivityItem(
    val title: String,
    val subtitle: String,
    val isRecent: Boolean
)
