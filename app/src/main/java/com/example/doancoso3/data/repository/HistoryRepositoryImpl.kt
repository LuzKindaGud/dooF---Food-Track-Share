package com.example.doancoso3.data.repository

import com.example.doancoso3.data.local.dao.HistoryEntryDao
import com.example.doancoso3.data.model.HistoryEntryEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val historyEntryDao: HistoryEntryDao
) : HistoryRepository {

    override fun getHistory(familyId: String, limit: Int): Flow<List<HistoryEntryEntity>> {
        // First, return local history
        // Then, optionally listen for remote changes if needed, but for simplicity we'll use Room + Sync
        return historyEntryDao.getHistoryByFamilyId(familyId, limit, 0)
    }

    override suspend fun logActivity(entry: HistoryEntryEntity): Result<Unit> {
        return try {
            val historyId = if (entry.id.isBlank()) UUID.randomUUID().toString() else entry.id
            val finalEntry = entry.copy(id = historyId, timestamp = System.currentTimeMillis())
            
            // Save locally
            historyEntryDao.insert(finalEntry)

            // Save to Firestore
            val historyMap = mapOf(
                "id" to finalEntry.id,
                "familyId" to finalEntry.familyId,
                "userId" to finalEntry.userId,
                "userName" to finalEntry.userName,
                "actionType" to finalEntry.actionType,
                "foodItemName" to finalEntry.foodItemName,
                "targetName" to finalEntry.targetName,
                "timestamp" to finalEntry.timestamp
            )

            firestore.collection("families")
                .document(finalEntry.familyId)
                .collection("history")
                .document(finalEntry.id)
                .set(historyMap)
                .await()

            historyEntryDao.insert(finalEntry.copy(synced = true))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncHistory(familyId: String): Result<Unit> {
        return try {
            val snapshot = firestore.collection("families")
                .document(familyId)
                .collection("history")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                val entry = mapHistoryEntry(doc.id, doc.data)
                if (entry != null) {
                    historyEntryDao.insert(entry.copy(synced = true))
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapHistoryEntry(id: String, data: Map<String, Any>?): HistoryEntryEntity? {
        if (data == null) return null
        return HistoryEntryEntity(
            id = id,
            familyId = data["familyId"] as? String ?: "",
            userId = data["userId"] as? String ?: "",
            userName = data["userName"] as? String ?: "User",
            actionType = data["actionType"] as? String ?: "",
            foodItemName = data["foodItemName"] as? String,
            targetName = data["targetName"] as? String,
            timestamp = data["timestamp"] as? Long ?: System.currentTimeMillis(),
            synced = true
        )
    }
}
