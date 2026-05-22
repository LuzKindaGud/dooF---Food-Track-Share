package com.example.doancoso3.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val type: String, // "JOIN_REQUEST"
    val senderId: String,
    val senderName: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean = false,
    val status: String = "PENDING" // "PENDING", "ACCEPTED", "DECLINED"
)
