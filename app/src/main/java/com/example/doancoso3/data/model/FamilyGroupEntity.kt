package com.example.doancoso3.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "family_groups")
data class FamilyGroupEntity(
    @PrimaryKey val id: String, // 6-char alphanumeric
    val name: String,
    val ownerId: String,
    val createdAt: Long
)
