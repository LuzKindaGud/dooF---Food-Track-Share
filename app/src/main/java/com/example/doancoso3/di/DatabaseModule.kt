package com.example.doancoso3.di

import android.content.Context
import androidx.room.Room
import com.example.doancoso3.data.local.AppDatabase
import com.example.doancoso3.data.local.dao.FamilyGroupDao
import com.example.doancoso3.data.local.dao.FoodItemDao
import com.example.doancoso3.data.local.dao.HistoryEntryDao
import com.example.doancoso3.data.local.dao.PendingSyncDao
import com.example.doancoso3.data.local.dao.ShoppingItemDao
import com.example.doancoso3.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "food_share_track_db"
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideFamilyGroupDao(database: AppDatabase): FamilyGroupDao {
        return database.familyGroupDao()
    }

    @Provides
    @Singleton
    fun provideFoodItemDao(database: AppDatabase): FoodItemDao {
        return database.foodItemDao()
    }

    @Provides
    @Singleton
    fun provideShoppingItemDao(database: AppDatabase): ShoppingItemDao {
        return database.shoppingItemDao()
    }

    @Provides
    @Singleton
    fun provideHistoryEntryDao(database: AppDatabase): HistoryEntryDao {
        return database.historyEntryDao()
    }

    @Provides
    @Singleton
    fun providePendingSyncDao(database: AppDatabase): PendingSyncDao {
        return database.pendingSyncDao()
    }
}
