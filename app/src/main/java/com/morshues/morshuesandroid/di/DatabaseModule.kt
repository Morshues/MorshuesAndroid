package com.morshues.morshuesandroid.di

import android.content.Context
import androidx.room.Room
import com.morshues.morshuesandroid.data.db.AppDatabase
import com.morshues.morshuesandroid.data.repository.SyncTaskRepository
import com.morshues.morshuesandroid.data.repository.SyncingFolderRepository

object DatabaseModule {
    private lateinit var applicationContext: Context

    fun init(context: Context) {
        applicationContext = context.applicationContext
    }

    private val appDatabase: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "morshues-db"
        ).build()
    }

    private val syncingFolderDao by lazy {
        appDatabase.syncingFolderDao()
    }

    val syncingFolderRepository: SyncingFolderRepository by lazy {
        SyncingFolderRepository(syncingFolderDao)
    }

    private val syncTaskDao by lazy {
        appDatabase.syncTaskDao()
    }

    val syncTaskRepository: SyncTaskRepository by lazy {
        SyncTaskRepository(syncTaskDao)
    }
}
