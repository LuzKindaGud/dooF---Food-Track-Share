package com.example.doancoso3.data.repository

import com.example.doancoso3.data.model.FoodItemEntity
import com.example.doancoso3.data.model.StorageLocation
import kotlinx.coroutines.flow.Flow

interface FoodItemRepository {
    fun getItems(familyId: String): Flow<List<FoodItemEntity>>
    fun getItemsByLocation(familyId: String, location: StorageLocation): Flow<List<FoodItemEntity>>
    fun searchItems(familyId: String, query: String): Flow<List<FoodItemEntity>>
    fun getExpiredItems(familyId: String): Flow<List<FoodItemEntity>>
    fun getExpiringSoonItems(familyId: String): Flow<List<FoodItemEntity>>
    
    suspend fun addItem(item: FoodItemEntity)
    suspend fun updateItem(item: FoodItemEntity)
    suspend fun deleteItem(itemId: String)
    
    suspend fun getUnsyncedItems(): List<FoodItemEntity>
    suspend fun markAsSynced(itemId: String)
}
