package com.example.doancoso3.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_items")
data class FoodItemEntity(
    @PrimaryKey val id: String,
    val familyId: String,
    val name: String,
    val quantity: Double,
    val expiryDate: Long,
    val storageLocation: String,
    val barcode: String?,
    val imageUri: String?,
    val createdBy: String,
    val createdAt: Long,
    val updatedAt: Long,
    val synced: Boolean = false,
    val deleted: Boolean = false
)
