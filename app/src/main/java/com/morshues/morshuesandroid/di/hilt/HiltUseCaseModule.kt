package com.morshues.morshuesandroid.di.hilt

import android.content.Context
import androidx.work.WorkManager
import com.morshues.morshuesandroid.data.repository.LocalFileRepository
import com.morshues.morshuesandroid.data.repository.RemoteFileRepository
import com.morshues.morshuesandroid.data.repository.SyncTaskRepository
import com.morshues.morshuesandroid.data.sync.SyncTaskEnqueuer
import com.morshues.morshuesandroid.domain.usecase.SyncFolderUseCase
import com.morshues.morshuesandroid.settings.SettingsManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module for use case and business logic components.
 * Provides use cases and task enqueuers (unscoped).
 */
@Module
@InstallIn(SingletonComponent::class)
object HiltUseCaseModule {

    @Provides
    fun provideSyncFolderUseCase(
        localFileRepository: LocalFileRepository,
        remoteFileRepository: RemoteFileRepository,
        syncTaskRepository: SyncTaskRepository,
        settingsManager: SettingsManager
    ): SyncFolderUseCase {
        return SyncFolderUseCase(
            localFileRepository,
            remoteFileRepository,
            syncTaskRepository,
            settingsManager
        )
    }

    @Provides
    fun provideSyncTaskEnqueuer(
        @ApplicationContext context: Context,
        workManager: WorkManager
    ): SyncTaskEnqueuer {
        return SyncTaskEnqueuer(context, workManager)
    }
}
