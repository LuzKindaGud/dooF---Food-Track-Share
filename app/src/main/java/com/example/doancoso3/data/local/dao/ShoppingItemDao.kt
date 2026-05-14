package com.example.doancoso3.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.doancoso3.data.model.ShoppingItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ShoppingItemEntity)

    @Query("DELETE FROM shopping_items WHERE id = :itemId")
    suspend fun deleteById(itemId: String)

    @Query("SELECT * FROM shopping_items WHERE familyId = :familyId ORDER BY addedAt DESC")
    fun getItemsByFamilyId(familyId: String): Flow<List<ShoppingItemEntity>>

    @Query("SELECT * FROM shopping_items WHERE synced = 0")
    suspend fun getUnsyncedItems(): List<ShoppingItemEntity>
}
