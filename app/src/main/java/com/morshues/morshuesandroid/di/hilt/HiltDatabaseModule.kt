package com.morshues.morshuesandroid.di.hilt

import android.content.Context
import androidx.room.Room
import com.morshues.morshuesandroid.data.db.AppDatabase
import com.morshues.morshuesandroid.data.db.dao.SyncTaskDao
import com.morshues.morshuesandroid.data.db.dao.SyncingFolderDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for database-related components.
 * Provides Room database and DAOs.
 */
@Module
@InstallIn(SingletonComponent::class)
object HiltDatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "morshues-db"
        ).build()
    }

    @Provides
    fun provideSyncingFolderDao(database: AppDatabase): SyncingFolderDao {
        return database.syncingFolderDao()
    }

    @Provides
    fun provideSyncTaskDao(database: AppDatabase): SyncTaskDao {
        return database.syncTaskDao()
    }
}
