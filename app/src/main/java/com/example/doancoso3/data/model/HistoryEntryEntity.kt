package com.example.doancoso3.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_entries")
data class HistoryEntryEntity(
    @PrimaryKey val id: String,
    val familyId: String,
    val userId: String,
    val userName: String,
    val actionType: String,
    val foodItemName: String? = null,
    val targetName: String? = null, // Can be food name or member name
    val timestamp: Long,
    val synced: Boolean = false
)
