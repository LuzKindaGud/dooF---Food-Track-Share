package com.example.doancoso3.data.repository

import com.example.doancoso3.data.local.dao.FamilyGroupDao
import com.example.doancoso3.data.local.dao.UserDao
import com.example.doancoso3.data.model.FamilyGroupEntity
import com.example.doancoso3.data.model.UserEntity
import com.example.doancoso3.utils.FamilyGroupIdGenerator
import com.example.doancoso3.utils.FamilyGroupValidator
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FamilyGroupRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val familyGroupDao: FamilyGroupDao,
    private val userDao: UserDao
) : FamilyGroupRepository {

    override suspend fun createGroup(ownerUserId: String): Result<FamilyGroupEntity> {
        return try {
            val userDoc = getUserDocument(ownerUserId)
            val currentFamilyId = userDoc.getString(FIELD_FAMILY_ID)
            if (!FamilyGroupValidator.canJoinGroup(currentFamilyId)) {
                return Result.failure(Exception("Bạn đã thuộc một nhóm gia đình"))
            }

            val familyId = generateUniqueFamilyId()
            val createdAt = System.currentTimeMillis()
            val group = FamilyGroupEntity(id = familyId, ownerId = ownerUserId, createdAt = createdAt)

            firestore.collection(COLLECTION_FAMILIES)
                .document(familyId)
                .set(
                    mapOf(
                        "id" to familyId,
                        "ownerId" to ownerUserId,
                        "createdAt" to createdAt
                    )
                )
                .await()

            firestore.collection(COLLECTION_USERS)
                .document(ownerUserId)
                .update(FIELD_FAMILY_ID, familyId)
                .await()

            familyGroupDao.insert(group)
            cacheUser(userDoc, familyId)

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

            val membersSnapshot = firestore.collection(COLLECTION_USERS)
                .whereEqualTo(FIELD_FAMILY_ID, normalizedId)
                .get()
                .await()
            if (!FamilyGroupValidator.canAddMember(membersSnapshot.size())) {
                return Result.failure(Exception("Nhóm đã đủ thành viên"))
            }

            firestore.collection(COLLECTION_USERS)
                .document(userId)
                .update(FIELD_FAMILY_ID, normalizedId)
                .await()

            val group = FamilyGroupEntity(
                id = normalizedId,
                ownerId = groupDoc.getString(FIELD_OWNER_ID) ?: "",
                createdAt = groupDoc.getLong(FIELD_CREATED_AT) ?: System.currentTimeMillis()
            )
            familyGroupDao.insert(group)
            cacheUser(userDoc, normalizedId)

            Result.success(group)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun leaveGroup(userId: String): Result<Unit> {
        return try {
            val userDoc = getUserDocument(userId)
            val currentFamilyId = userDoc.getString(FIELD_FAMILY_ID)
            if (currentFamilyId.isNullOrBlank()) {
                return Result.success(Unit)
            }

            firestore.collection(COLLECTION_USERS)
                .document(userId)
                .update(FIELD_FAMILY_ID, null)
                .await()

            userDao.updateFamilyId(userId, null)
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
            if (memberFamilyId != ownerFamilyId) {
                return Result.failure(Exception("Thành viên không thuộc nhóm này"))
            }

            firestore.collection(COLLECTION_USERS)
                .document(memberId)
                .update(FIELD_FAMILY_ID, null)
                .await()

            userDao.updateFamilyId(memberId, null)
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
                    familyGroupDao.observeGroupById(familyId)
                }
            }
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
        val existing = userDao.getUserById(user.id)
        if (existing == null) {
            userDao.insert(user)
        } else {
            userDao.updateFamilyId(user.id, familyId)
        }
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
        val createdAt = (data[FIELD_CREATED_AT] as? com.google.firebase.Timestamp)
            ?.toDate()
            ?.time
            ?: System.currentTimeMillis()
        return UserEntity(
            id = data[FIELD_UID] as? String ?: documentId,
            email = email,
            displayName = displayName,
            familyId = familyId,
            createdAt = createdAt
        )
    }

    private companion object {
        const val COLLECTION_USERS = "users"
        const val COLLECTION_FAMILIES = "families"

        const val FIELD_UID = "uid"
        const val FIELD_EMAIL = "email"
        const val FIELD_DISPLAY_NAME = "displayName"
        const val FIELD_FAMILY_ID = "familyId"
        const val FIELD_OWNER_ID = "ownerId"
        const val FIELD_CREATED_AT = "createdAt"
    }
}
