package com.example.doancoso3.data.repository

import com.example.doancoso3.data.model.FamilyGroupEntity
import com.example.doancoso3.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

interface FamilyGroupRepository {

    suspend fun createGroup(ownerUserId: String): Result<FamilyGroupEntity>

    suspend fun joinGroup(userId: String, familyId: String): Result<FamilyGroupEntity>

    suspend fun leaveGroup(userId: String): Result<Unit>

    suspend fun removeMember(ownerId: String, memberId: String): Result<Unit>

    fun getGroupMembers(familyId: String): Flow<List<UserEntity>>

    fun getUserGroup(userId: String): Flow<FamilyGroupEntity?>

    suspend fun refreshGroupMembers(familyId: String): Result<Unit>
}
