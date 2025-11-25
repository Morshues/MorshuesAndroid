package com.morshues.morshuesandroid.di

import android.content.Context
import androidx.work.WorkManager
import com.morshues.morshuesandroid.data.SessionStore
import com.morshues.morshuesandroid.data.repository.AuthRepository
import com.morshues.morshuesandroid.data.repository.LocalFileRepository
import com.morshues.morshuesandroid.data.repository.RemoteFileRepository
import com.morshues.morshuesandroid.data.repository.SyncTaskRepository
import com.morshues.morshuesandroid.data.repository.SyncingFolderRepository
import com.morshues.morshuesandroid.data.sync.SyncTaskEnqueuer
import com.morshues.morshuesandroid.domain.usecase.SyncFolderUseCase
import com.morshues.morshuesandroid.settings.SettingsManager

/**
 * A simple manual dependency injector to provide singleton instances of our services.
 * In a larger app, this would be replaced by Hilt.
 */
object AppModule {
    private lateinit var applicationContext: Context

    fun init(context: Context) {
        applicationContext = context.applicationContext
        DatabaseModule.init(applicationContext)
    }

    val settingsManager: SettingsManager by lazy {
        SettingsManager(applicationContext)
    }

    val sessionStore: SessionStore by lazy {
        SessionStore(applicationContext)
    }

    private val authApiService by lazy {
        NetworkModule.createAuthApiService(settingsManager)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(authApiService)
    }

    private val apiService by lazy {
        NetworkModule.createApiService(settingsManager, sessionStore, authRepository)
    }

    val remoteFileRepository: RemoteFileRepository by lazy {
        RemoteFileRepository(apiService)
    }

    val localFileRepository: LocalFileRepository by lazy {
        LocalFileRepository(applicationContext)
    }

    val syncingFolderRepository: SyncingFolderRepository by lazy {
        DatabaseModule.syncingFolderRepository
    }

    val syncTaskRepository: SyncTaskRepository by lazy {
        DatabaseModule.syncTaskRepository
    }

    val syncTaskEnqueuer: SyncTaskEnqueuer by lazy {
        SyncTaskEnqueuer(applicationContext, workManager)
    }

    val syncFolderUseCase: SyncFolderUseCase by lazy {
        SyncFolderUseCase(localFileRepository, remoteFileRepository, syncTaskRepository, settingsManager)
    }

    val workManager: WorkManager by lazy {
        WorkManager.getInstance(applicationContext)
    }
}