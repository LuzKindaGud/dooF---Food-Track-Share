package com.example.doancoso3.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.doancoso3.data.local.dao.FamilyGroupDao
import com.example.doancoso3.data.local.dao.FoodItemDao
import com.example.doancoso3.data.local.dao.HistoryEntryDao
import com.example.doancoso3.data.local.dao.PendingSyncDao
import com.example.doancoso3.data.local.dao.ShoppingItemDao
import com.example.doancoso3.data.local.dao.UserDao
import com.example.doancoso3.data.local.dao.NotificationDao
import com.example.doancoso3.data.model.Converters
import com.example.doancoso3.data.model.FamilyGroupEntity
import com.example.doancoso3.data.model.FoodItemEntity
import com.example.doancoso3.data.model.HistoryEntryEntity
import com.example.doancoso3.data.model.PendingSyncEntity
import com.example.doancoso3.data.model.ShoppingItemEntity
import com.example.doancoso3.data.model.UserEntity
import com.example.doancoso3.data.model.NotificationEntity

@Database(
    entities = [
        UserEntity::class,
        FamilyGroupEntity::class,
        FoodItemEntity::class,
        ShoppingItemEntity::class,
        HistoryEntryEntity::class,
        PendingSyncEntity::class,
        NotificationEntity::class
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun familyGroupDao(): FamilyGroupDao
    abstract fun foodItemDao(): FoodItemDao
    abstract fun shoppingItemDao(): ShoppingItemDao
    abstract fun historyEntryDao(): HistoryEntryDao
    abstract fun pendingSyncDao(): PendingSyncDao
    abstract fun notificationDao(): NotificationDao
}
