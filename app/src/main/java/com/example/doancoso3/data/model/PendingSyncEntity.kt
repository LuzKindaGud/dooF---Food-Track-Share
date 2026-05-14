package com.example.doancoso3.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_sync_operations")
data class PendingSyncEntity(
    @PrimaryKey val id: String,
    val entityType: String,
    val entityId: String,
    val operationType: String,
    val payload: String,
    val createdAt: Long,
    val retryCount: Int = 0
)
