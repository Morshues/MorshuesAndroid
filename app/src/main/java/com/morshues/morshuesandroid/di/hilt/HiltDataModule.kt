package com.morshues.morshuesandroid.di.hilt

import android.content.Context
import androidx.work.WorkManager
import com.morshues.morshuesandroid.data.SessionStore
import com.morshues.morshuesandroid.settings.SettingsManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for application-level data components.
 * Provides SessionStore, SettingsManager, and WorkManager.
 */
@Module
@InstallIn(SingletonComponent::class)
object HiltDataModule {

    @Provides
    @Singleton
    fun provideSessionStore(
        @ApplicationContext context: Context
    ): SessionStore {
        return SessionStore(context)
    }

    @Provides
    @Singleton
    fun provideSettingsManager(
        @ApplicationContext context: Context
    ): SettingsManager {
        return SettingsManager(context)
    }

    @Provides
    @Singleton
    fun provideWorkManager(
        @ApplicationContext context: Context
    ): WorkManager {
        return WorkManager.getInstance(context)
    }
}
