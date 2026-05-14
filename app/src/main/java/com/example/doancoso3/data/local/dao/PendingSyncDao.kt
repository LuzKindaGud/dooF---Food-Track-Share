package com.example.doancoso3.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.doancoso3.data.model.PendingSyncEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingSyncDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(operation: PendingSyncEntity)

    @Query("DELETE FROM pending_sync_operations WHERE id = :operationId")
    suspend fun deleteById(operationId: String)

    @Query("SELECT * FROM pending_sync_operations ORDER BY createdAt ASC")
    suspend fun getAllPending(): List<PendingSyncEntity>

    @Query("SELECT COUNT(*) FROM pending_sync_operations")
    fun getPendingCount(): Flow<Int>
}
