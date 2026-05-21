package com.example.doancoso3.di

import com.example.doancoso3.data.repository.AuthRepository
import com.example.doancoso3.data.repository.AuthRepositoryImpl
import com.example.doancoso3.data.repository.FamilyGroupRepository
import com.example.doancoso3.data.repository.FamilyGroupRepositoryImpl
import com.example.doancoso3.data.repository.FoodItemRepository
import com.example.doancoso3.data.repository.FoodItemRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindFamilyGroupRepository(impl: FamilyGroupRepositoryImpl): FamilyGroupRepository

    @Binds
    @Singleton
    abstract fun bindFoodItemRepository(impl: FoodItemRepositoryImpl): FoodItemRepository

    // @Binds
    // @Singleton
    // abstract fun bindShoppingListRepository(impl: ShoppingListRepositoryImpl): ShoppingListRepository
    //
    // @Binds
    // @Singleton
    // abstract fun bindActivityHistoryRepository(impl: ActivityHistoryRepositoryImpl): ActivityHistoryRepository
    //
    // @Binds
    // @Singleton
    // abstract fun bindSyncManager(impl: SyncManagerImpl): SyncManager
}
