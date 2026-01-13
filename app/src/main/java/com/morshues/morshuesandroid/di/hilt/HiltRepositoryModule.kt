package com.morshues.morshuesandroid.di.hilt

import android.content.Context
import com.morshues.morshuesandroid.data.api.ApiService
import com.morshues.morshuesandroid.data.api.AuthApiService
import com.morshues.morshuesandroid.data.db.dao.SyncTaskDao
import com.morshues.morshuesandroid.data.db.dao.SyncingFolderDao
import com.morshues.morshuesandroid.data.repository.AuthRepository
import com.morshues.morshuesandroid.data.repository.LocalFileRepository
import com.morshues.morshuesandroid.data.repository.RemoteFileRepository
import com.morshues.morshuesandroid.data.repository.SyncTaskRepository
import com.morshues.morshuesandroid.data.repository.SyncingFolderRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module for repository components.
 * Provides all repository instances (unscoped for lightweight, stateless objects).
 */
@Module
@InstallIn(SingletonComponent::class)
object HiltRepositoryModule {

    @Provides
    fun provideAuthRepository(
        authApiService: AuthApiService
    ): AuthRepository {
        return AuthRepository(authApiService)
    }

    @Provides
    fun provideRemoteFileRepository(
        apiService: ApiService
    ): RemoteFileRepository {
        return RemoteFileRepository(apiService)
    }

    @Provides
    fun provideLocalFileRepository(
        @ApplicationContext context: Context
    ): LocalFileRepository {
        return LocalFileRepository(context)
    }

    @Provides
    fun provideSyncingFolderRepository(
        syncingFolderDao: SyncingFolderDao
    ): SyncingFolderRepository {
        return SyncingFolderRepository(syncingFolderDao)
    }

    @Provides
    fun provideSyncTaskRepository(
        syncTaskDao: SyncTaskDao
    ): SyncTaskRepository {
        return SyncTaskRepository(syncTaskDao)
    }
}
