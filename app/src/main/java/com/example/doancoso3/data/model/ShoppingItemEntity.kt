package com.example.doancoso3.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_items")
data class ShoppingItemEntity(
    @PrimaryKey val id: String,
    val familyId: String,
    val name: String,
    val quantity: Int?,
    val addedBy: String,
    val addedAt: Long,
    val synced: Boolean = false
)
