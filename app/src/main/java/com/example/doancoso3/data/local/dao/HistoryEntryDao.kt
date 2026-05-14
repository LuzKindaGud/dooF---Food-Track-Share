package com.example.doancoso3.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.doancoso3.data.model.HistoryEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: HistoryEntryEntity)

    @Query("SELECT * FROM history_entries WHERE familyId = :familyId ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    fun getHistoryByFamilyId(familyId: String, limit: Int, offset: Int): Flow<List<HistoryEntryEntity>>

    @Query("SELECT * FROM history_entries WHERE synced = 0")
    suspend fun getUnsyncedEntries(): List<HistoryEntryEntity>
}
