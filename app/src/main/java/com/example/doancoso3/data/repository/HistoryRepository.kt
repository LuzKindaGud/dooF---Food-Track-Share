package com.example.doancoso3.data.repository

import com.example.doancoso3.data.model.HistoryEntryEntity
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun getHistory(familyId: String, limit: Int = 10): Flow<List<HistoryEntryEntity>>
    suspend fun logActivity(entry: HistoryEntryEntity): Result<Unit>
    suspend fun syncHistory(familyId: String): Result<Unit>
}
