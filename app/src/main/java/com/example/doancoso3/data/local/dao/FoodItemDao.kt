package com.example.doancoso3.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.doancoso3.data.model.FoodItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: FoodItemEntity)

    @Update
    suspend fun update(item: FoodItemEntity)

    @Query("UPDATE food_items SET deleted = 1, synced = 0 WHERE id = :itemId")
    suspend fun softDelete(itemId: String)

    @Query("SELECT * FROM food_items WHERE familyId = :familyId AND deleted = 0")
    fun getItemsByFamilyId(familyId: String): Flow<List<FoodItemEntity>>

    @Query("SELECT * FROM food_items WHERE familyId = :familyId AND storageLocation = :location AND deleted = 0")
    fun getItemsByLocation(familyId: String, location: String): Flow<List<FoodItemEntity>>

    @Query("SELECT * FROM food_items WHERE familyId = :familyId AND name LIKE '%' || :query || '%' AND deleted = 0")
    fun searchByName(familyId: String, query: String): Flow<List<FoodItemEntity>>

    @Query("SELECT * FROM food_items WHERE familyId = :familyId AND deleted = 0 AND expiryDate < :thresholdMillis")
    fun getExpiredItems(familyId: String, thresholdMillis: Long): Flow<List<FoodItemEntity>>

    @Query("SELECT * FROM food_items WHERE familyId = :familyId AND deleted = 0 AND expiryDate BETWEEN :startMillis AND :endMillis")
    fun getExpiringSoonItems(familyId: String, startMillis: Long, endMillis: Long): Flow<List<FoodItemEntity>>

    @Query("SELECT * FROM food_items WHERE synced = 0")
    suspend fun getUnsyncedItems(): List<FoodItemEntity>

    @Query("SELECT * FROM food_items WHERE id = :itemId")
    suspend fun getItemById(itemId: String): FoodItemEntity?
}
