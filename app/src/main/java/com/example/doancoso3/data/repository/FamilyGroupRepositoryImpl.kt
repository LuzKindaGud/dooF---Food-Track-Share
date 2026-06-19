package com.example.doancoso3.data.repository

import com.example.doancoso3.data.local.dao.FamilyGroupDao
import com.example.doancoso3.data.local.dao.NotificationDao
import com.example.doancoso3.data.local.dao.UserDao
import com.example.doancoso3.data.model.FamilyGroupEntity
import com.example.doancoso3.data.model.HistoryEntryEntity
import com.example.doancoso3.data.model.NotificationEntity
import com.example.doancoso3.data.model.UserEntity
import com.example.doancoso3.utils.FamilyGroupIdGenerator
import com.example.doancoso3.utils.FamilyGroupValidator
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FamilyGroupRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val familyGroupDao: FamilyGroupDao,
    private val userDao: UserDao,
    private val notificationDao: NotificationDao,
    private val historyRepository: HistoryRepository
) : FamilyGroupRepository {

    override suspend fun createGroup(ownerUserId: String, name: String): Result<FamilyGroupEntity> {
        return try {
            val userDoc = getUserDocument(ownerUserId)
            val currentFamilyId = userDoc.getString(FIELD_FAMILY_ID)
            val userName = userDoc.getString(FIELD_DISPLAY_NAME) ?: "User"
            if (!FamilyGroupValidator.canJoinGroup(currentFamilyId)) {
                return Result.failure(Exception("Bạn đã thuộc một nhóm gia đình"))
            }

            val familyId = generateUniqueFamilyId()
            val createdAt = System.currentTimeMillis()
            val group = FamilyGroupEntity(id = familyId, name = name, ownerId = ownerUserId, createdAt = createdAt)

            firestore.collection(COLLECTION_FAMILIES)
                .document(familyId)
                .set(
                    mapOf(
                        "id" to familyId,
                        "name" to name,
                        "ownerId" to ownerUserId,
                        "createdAt" to createdAt
                    )
                )
                .await()

            // Update user's familyId in Firestore
            firestore.collection(COLLECTION_USERS)
                .document(ownerUserId)
                .update(FIELD_FAMILY_ID, familyId)
                .await()

            // Save group locally
            familyGroupDao.insert(group)
            
            // Update local user and ensure they are cached with the new familyId
            cacheUser(userDoc, familyId)

            // Log history
            historyRepository.logActivity(
                HistoryEntryEntity(
                    id = "",
                    familyId = familyId,
                    userId = ownerUserId,
                    userName = userName,
                    actionType = "JOINED",
                    targetName = name,
                    timestamp = createdAt
                )
            )

            Result.success(group)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun joinGroup(userId: String, familyId: String): Result<FamilyGroupEntity> {
        return try {
            val normalizedId = familyId.trim().uppercase()
            if (!FamilyGroupValidator.isFamilyIdFormatValid(normalizedId)) {
                return Result.failure(Exception("Family ID không hợp lệ"))
            }

            val groupDoc = firestore.collection(COLLECTION_FAMILIES)
                .document(normalizedId)
                .get()
                .await()
            if (!groupDoc.exists()) {
                return Result.failure(Exception("Không tìm thấy nhóm gia đình"))
            }

            val userDoc = getUserDocument(userId)
            val currentFamilyId = userDoc.getString(FIELD_FAMILY_ID)
            if (!FamilyGroupValidator.canJoinGroup(currentFamilyId)) {
                return Result.failure(Exception("Bạn đã thuộc một nhóm gia đình"))
            }

            val senderName = userDoc.getString(FIELD_DISPLAY_NAME) ?: "User"

            // Instead of direct join, send a request
            val requestResult = sendJoinRequest(userId, senderName, normalizedId)
            
            if (requestResult.isSuccess) {
                Result.failure(Exception("Đã gửi yêu cầu tham gia. Vui lòng chờ chủ nhóm duyệt."))
            } else {
                Result.failure(requestResult.exceptionOrNull() ?: Exception("Gửi yêu cầu thất bại"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun leaveGroup(userId: String): Result<Unit> {
        return try {
            val userDoc = getUserDocument(userId)
            val currentFamilyId = userDoc.getString(FIELD_FAMILY_ID)
            val userName = userDoc.getString(FIELD_DISPLAY_NAME) ?: "User"
            if (currentFamilyId.isNullOrBlank()) {
                return Result.success(Unit)
            }

            firestore.collection(COLLECTION_USERS)
                .document(userId)
                .update(FIELD_FAMILY_ID, null)
                .await()

            userDao.updateFamilyId(userId, null)

            // Log history
            historyRepository.logActivity(
                HistoryEntryEntity(
                    id = "",
                    familyId = currentFamilyId,
                    userId = userId,
                    userName = userName,
                    actionType = "LEFT",
                    timestamp = System.currentTimeMillis()
                )
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeMember(ownerId: String, memberId: String): Result<Unit> {
        return try {
            if (ownerId == memberId) {
                return Result.failure(Exception("Không thể tự xóa chính mình"))
            }

            val ownerDoc = getUserDocument(ownerId)
            val ownerFamilyId = ownerDoc.getString(FIELD_FAMILY_ID)
            if (ownerFamilyId.isNullOrBlank()) {
                return Result.failure(Exception("Bạn chưa thuộc nhóm nào"))
            }

            val groupDoc = firestore.collection(COLLECTION_FAMILIES)
                .document(ownerFamilyId)
                .get()
                .await()
            val groupOwnerId = groupDoc.getString(FIELD_OWNER_ID)
            if (groupOwnerId != ownerId) {
                return Result.failure(Exception("Chỉ chủ nhóm mới có thể xóa thành viên"))
            }

            val memberDoc = getUserDocument(memberId)
            val memberFamilyId = memberDoc.getString(FIELD_FAMILY_ID)
            val memberName = memberDoc.getString(FIELD_DISPLAY_NAME) ?: "User"
            if (memberFamilyId != ownerFamilyId) {
                return Result.failure(Exception("Thành viên không thuộc nhóm này"))
            }

            firestore.collection(COLLECTION_USERS)
                .document(memberId)
                .update(FIELD_FAMILY_ID, null)
                .await()

            userDao.updateFamilyId(memberId, null)

            // Log history
            historyRepository.logActivity(
                HistoryEntryEntity(
                    id = "",
                    familyId = ownerFamilyId,
                    userId = ownerId,
                    userName = memberName,
                    actionType = "REMOVED",
                    timestamp = System.currentTimeMillis()
                )
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getGroupMembers(familyId: String): Flow<List<UserEntity>> {
        return familyGroupDao.getGroupMembers(familyId)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getUserGroup(userId: String): Flow<FamilyGroupEntity?> {
        return userDao.observeUserById(userId)
            .flatMapLatest { user ->
                val familyId = user?.familyId
                if (familyId.isNullOrBlank()) {
                    flowOf(null)
                } else {
                    // Emit the locally cached group, and keep it in sync with Firestore so that
                    // after a fresh login (where only the user's familyId is synced) the group
                    // record and its members are pulled into Room and shown.
                    callbackFlow {
                        val roomJob = launch {
                            familyGroupDao.observeGroupById(familyId).collect { trySend(it) }
                        }
                        val registration = firestore.collection(COLLECTION_FAMILIES)
                            .document(familyId)
                            .addSnapshotListener { snapshot, _ ->
                                if (snapshot != null && snapshot.exists()) {
                                    launch {
                                        upsertGroupFromSnapshot(snapshot)
                                        refreshGroupMembers(familyId)
                                    }
                                }
                            }
                        awaitClose {
                            registration.remove()
                            roomJob.cancel()
                        }
                    }
                }
            }
    }

    /** Maps a Firestore family document into a Room entity and upserts it. */
    private suspend fun upsertGroupFromSnapshot(snapshot: com.google.firebase.firestore.DocumentSnapshot) {
        val id = snapshot.getString("id") ?: snapshot.id
        val name = snapshot.getString("name") ?: ""
        val ownerId = snapshot.getString(FIELD_OWNER_ID) ?: ""
        val createdAt = when (val raw = snapshot.get(FIELD_CREATED_AT)) {
            is com.google.firebase.Timestamp -> raw.toDate().time
            is Number -> raw.toLong()
            else -> System.currentTimeMillis()
        }
        familyGroupDao.insert(
            FamilyGroupEntity(id = id, name = name, ownerId = ownerId, createdAt = createdAt)
        )
    }

    override suspend fun refreshGroupMembers(familyId: String): Result<Unit> {
        return try {
            val snapshot = firestore.collection(COLLECTION_USERS)
                .whereEqualTo(FIELD_FAMILY_ID, familyId)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                val user = mapUser(doc.id, doc.data)
                if (user != null) {
                    userDao.insert(user)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendJoinRequest(userId: String, senderName: String, familyId: String): Result<Unit> {
        return try {
            val groupDoc = firestore.collection(COLLECTION_FAMILIES)
                .document(familyId)
                .get()
                .await()
            val ownerId = groupDoc.getString(FIELD_OWNER_ID) ?: return Result.failure(Exception("Không tìm thấy chủ nhóm"))

            val notificationId = firestore.collection(COLLECTION_NOTIFICATIONS).document().id
            val timestamp = System.currentTimeMillis()
            
            val notiMap = mapOf(
                "id" to notificationId,
                "type" to "JOIN_REQUEST",
                "senderId" to userId,
                "senderName" to senderName,
                "message" to "wants to join your Family Group",
                "timestamp" to timestamp,
                "status" to "PENDING",
                "receiverId" to ownerId,
                "familyId" to familyId
            )

            firestore.collection(COLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .set(notiMap)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun respondToJoinRequest(notificationId: String, accept: Boolean): Result<Unit> {
        return try {
            val notiDoc = firestore.collection(COLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .get()
                .await()
            
            if (!notiDoc.exists()) return Result.failure(Exception("Không tìm thấy yêu cầu"))

            val senderId = notiDoc.getString("senderId") ?: ""
            val senderName = notiDoc.getString("senderName") ?: "User"
            val familyId = notiDoc.getString("familyId") ?: ""
            val newStatus = if (accept) "ACCEPTED" else "DECLINED"

            // Update notification status
            firestore.collection(COLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .update("status", newStatus)
                .await()

            if (accept) {
                // Add user to family
                firestore.collection(COLLECTION_USERS)
                    .document(senderId)
                    .update(FIELD_FAMILY_ID, familyId)
                    .await()
                
                // Trigger members refresh
                refreshGroupMembers(familyId)

                // Log history
                historyRepository.logActivity(
                    HistoryEntryEntity(
                        id = "",
                        familyId = familyId,
                        userId = senderId,
                        userName = senderName,
                        actionType = "JOINED",
                        timestamp = System.currentTimeMillis()
                    )
                )
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeNotifications(userId: String): Flow<List<NotificationEntity>> {
        return callbackFlow {
            val listener = firestore.collection(COLLECTION_NOTIFICATIONS)
                .whereEqualTo("receiverId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }
                    val notifications = snapshot?.documents?.mapNotNull { doc ->
                        mapNotification(doc.id, doc.data)
                    } ?: emptyList()
                    
                    launch {
                        notifications.forEach { notificationDao.insert(it) }
                    }
                    trySend(notifications)
                }
            awaitClose { listener.remove() }
        }
    }

    override suspend fun syncNotifications(userId: String): Result<Unit> {
        return try {
            val snapshot = firestore.collection(COLLECTION_NOTIFICATIONS)
                .whereEqualTo("receiverId", userId)
                .get()
                .await()
            snapshot.documents.forEach { doc ->
                mapNotification(doc.id, doc.data)?.let { notificationDao.insert(it) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapNotification(id: String, data: Map<String, Any>?): NotificationEntity? {
        if (data == null) return null
        return NotificationEntity(
            id = id,
            type = data["type"] as? String ?: "",
            senderId = data["senderId"] as? String ?: "",
            senderName = data["senderName"] as? String ?: "",
            message = data["message"] as? String ?: "",
            timestamp = data["timestamp"] as? Long ?: System.currentTimeMillis(),
            isRead = data["isRead"] as? Boolean ?: false,
            status = data["status"] as? String ?: "PENDING"
        )
    }

    private suspend fun generateUniqueFamilyId(): String {
        repeat(5) {
            val candidate = FamilyGroupIdGenerator.generate()
            val existing = firestore.collection(COLLECTION_FAMILIES)
                .document(candidate)
                .get()
                .await()
            if (!existing.exists()) {
                return candidate
            }
        }
        throw IllegalStateException("Không thể tạo Family ID, vui lòng thử lại")
    }

    private suspend fun getUserDocument(userId: String) =
        firestore.collection(COLLECTION_USERS).document(userId).get().await().also { doc ->
            if (!doc.exists()) {
                throw IllegalStateException("Không tìm thấy người dùng")
            }
        }

    private suspend fun cacheUser(userDoc: com.google.firebase.firestore.DocumentSnapshot, familyId: String?) {
        val user = mapUser(userDoc.id, userDoc.data, familyId) ?: return
        userDao.insert(user)
    }

    private fun mapUser(
        documentId: String,
        data: Map<String, Any>?,
        overrideFamilyId: String? = null
    ): UserEntity? {
        if (data == null) return null
        val email = data[FIELD_EMAIL] as? String ?: ""
        val displayName = data[FIELD_DISPLAY_NAME] as? String ?: ""
        val familyId = overrideFamilyId ?: data[FIELD_FAMILY_ID] as? String
        
        // Handle Timestamp correctly
        val createdAt = when (val raw = data[FIELD_CREATED_AT]) {
            is com.google.firebase.Timestamp -> raw.toDate().time
            is Long -> raw
            else -> System.currentTimeMillis()
        }
        
        return UserEntity(
            id = data["id"] as? String ?: documentId,
            email = email,
            displayName = displayName,
            familyId = familyId,
            createdAt = createdAt
        )
    }

    private companion object {
        const val COLLECTION_USERS = "users"
        const val COLLECTION_FAMILIES = "families"
        const val COLLECTION_NOTIFICATIONS = "notifications"

        const val FIELD_UID = "uid"
        const val FIELD_EMAIL = "email"
        const val FIELD_DISPLAY_NAME = "displayName"
        const val FIELD_FAMILY_ID = "familyId"
        const val FIELD_OWNER_ID = "ownerId"
        const val FIELD_CREATED_AT = "createdAt"
    }
}
