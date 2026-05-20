package com.example.doancoso3.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.doancoso3.data.model.FamilyGroupEntity
import com.example.doancoso3.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyGroupDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(group: FamilyGroupEntity)

    @Query("SELECT * FROM family_groups WHERE id = :groupId")
    suspend fun getGroupById(groupId: String): FamilyGroupEntity?

    @Query("SELECT * FROM family_groups WHERE id = :groupId")
    fun observeGroupById(groupId: String): Flow<FamilyGroupEntity?>

    @Query("SELECT * FROM users WHERE familyId = :familyId")
    fun getGroupMembers(familyId: String): Flow<List<UserEntity>>
}
