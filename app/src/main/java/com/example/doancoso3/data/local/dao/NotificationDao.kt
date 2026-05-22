package com.example.doancoso3.data.local.dao

import androidx.room.*
import com.example.doancoso3.data.model.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity)

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE status = 'PENDING' ORDER BY timestamp DESC")
    fun getPendingNotifications(): Flow<List<NotificationEntity>>

    @Update
    suspend fun update(notification: NotificationEntity)

    @Delete
    suspend fun delete(notification: NotificationEntity)

    @Query("DELETE FROM notifications")
    suspend fun deleteAll()
}
