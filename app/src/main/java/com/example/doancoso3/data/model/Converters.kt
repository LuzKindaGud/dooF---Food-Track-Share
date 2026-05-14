package com.example.doancoso3.data.model

import androidx.room.TypeConverter
import java.util.Date

class Converters {

    // Date converters
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // StorageLocation converters
    @TypeConverter
    fun fromStorageLocation(value: StorageLocation): String {
        return value.name
    }

    @TypeConverter
    fun toStorageLocation(value: String): StorageLocation {
        return StorageLocation.valueOf(value)
    }

    // ExpiryStatus converters
    @TypeConverter
    fun fromExpiryStatus(value: ExpiryStatus): String {
        return value.name
    }

    @TypeConverter
    fun toExpiryStatus(value: String): ExpiryStatus {
        return ExpiryStatus.valueOf(value)
    }

    // ActionType converters
    @TypeConverter
    fun fromActionType(value: ActionType): String {
        return value.name
    }

    @TypeConverter
    fun toActionType(value: String): ActionType {
        return ActionType.valueOf(value)
    }

    // SyncStatus converters
    @TypeConverter
    fun fromSyncStatus(value: SyncStatus): String {
        return value.name
    }

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus {
        return SyncStatus.valueOf(value)
    }
}
