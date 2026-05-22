package com.example.doancoso3.data.repository

import com.example.doancoso3.data.local.dao.FoodItemDao
import com.example.doancoso3.data.local.dao.PendingSyncDao
import com.example.doancoso3.data.local.dao.UserDao
import com.example.doancoso3.data.model.FoodItemEntity
import com.example.doancoso3.data.model.HistoryEntryEntity
import com.example.doancoso3.data.model.PendingSyncEntity
import com.example.doancoso3.data.model.StorageLocation
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import com.example.doancoso3.data.repository.HistoryRepository

@Singleton
class FoodItemRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val foodItemDao: FoodItemDao,
    private val pendingSyncDao: PendingSyncDao,
    private val userDao: UserDao,
    private val historyRepository: HistoryRepository
) : FoodItemRepository {

    override fun getItems(familyId: String): Flow<List<FoodItemEntity>> {
        return foodItemDao.getItemsByFamilyId(familyId)
    }

    override fun getItemsByLocation(familyId: String, location: StorageLocation): Flow<List<FoodItemEntity>> {
        return foodItemDao.getItemsByLocation(familyId, location.name)
    }

    override fun searchItems(familyId: String, query: String): Flow<List<FoodItemEntity>> {
        return foodItemDao.searchByName(familyId, query)
    }

    override fun getExpiredItems(familyId: String): Flow<List<FoodItemEntity>> {
        val now = System.currentTimeMillis()
        return foodItemDao.getExpiredItems(familyId, now)
    }

    override fun getExpiringSoonItems(familyId: String): Flow<List<FoodItemEntity>> {
        val now = System.currentTimeMillis()
        val twoDaysMillis = 2 * 24 * 60 * 60 * 1000L
        return foodItemDao.getExpiringSoonItems(familyId, now, now + twoDaysMillis)
    }

    override suspend fun addItem(item: FoodItemEntity) {
        foodItemDao.insert(item)
        syncToFirestore(item, "ADD")
        
        // Log history
        val user = userDao.getUserById(item.createdBy)
        historyRepository.logActivity(
            HistoryEntryEntity(
                id = "",
                familyId = item.familyId,
                userId = item.createdBy,
                userName = user?.displayName ?: "User",
                actionType = "ADDED",
                foodItemName = item.name,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    override suspend fun updateItem(item: FoodItemEntity) {
        foodItemDao.update(item)
        syncToFirestore(item, "UPDATE")

        // Log history
        val user = userDao.getUserById(item.createdBy) // Ideally we'd use the current editor's ID
        historyRepository.logActivity(
            HistoryEntryEntity(
                id = "",
                familyId = item.familyId,
                userId = item.createdBy,
                userName = user?.displayName ?: "User",
                actionType = "EDITED",
                foodItemName = item.name,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    override suspend fun deleteItem(itemId: String) {
        val item = foodItemDao.getItemById(itemId) ?: return
        foodItemDao.softDelete(itemId)
        
        // Log history
        val user = userDao.getUserById(item.createdBy)
        historyRepository.logActivity(
            HistoryEntryEntity(
                id = "",
                familyId = item.familyId,
                userId = item.createdBy,
                userName = user?.displayName ?: "User",
                actionType = "DELETED",
                foodItemName = item.name,
                timestamp = System.currentTimeMillis()
            )
        )

        val pendingSync = PendingSyncEntity(
            id = UUID.randomUUID().toString(),
            entityType = "FOOD_ITEM",
            entityId = itemId,
            operationType = "DELETE",
            payload = "",
            createdAt = System.currentTimeMillis()
        )
        pendingSyncDao.insert(pendingSync)
    }

    private suspend fun syncToFirestore(item: FoodItemEntity, operation: String) {
        try {
            val itemMap = mapOf(
                "id" to item.id,
                "familyId" to item.familyId,
                "name" to item.name,
                "quantity" to item.quantity,
                "expiryDate" to item.expiryDate,
                "storageLocation" to item.storageLocation,
                "barcode" to item.barcode,
                "imageUri" to item.imageUri,
                "createdBy" to item.createdBy,
                "createdAt" to item.createdAt,
                "updatedAt" to item.updatedAt,
                "deleted" to item.deleted
            )

            firestore.collection("families")
                .document(item.familyId)
                .collection("food_items")
                .document(item.id)
                .set(itemMap)
                .await()
            
            foodItemDao.update(item.copy(synced = true))
        } catch (e: Exception) {
            val pendingSync = PendingSyncEntity(
                id = UUID.randomUUID().toString(),
                entityType = "FOOD_ITEM",
                entityId = item.id,
                operationType = operation,
                payload = "", // Payload would normally be JSON
                createdAt = System.currentTimeMillis()
            )
            pendingSyncDao.insert(pendingSync)
        }
    }

    override suspend fun getUnsyncedItems(): List<FoodItemEntity> {
        return foodItemDao.getUnsyncedItems()
    }

    override suspend fun markAsSynced(itemId: String) {
        // This would be used by SyncWorker
    }
}
