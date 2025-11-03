package com.morshues.morshuesandroid.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.morshues.morshuesandroid.data.db.dao.SyncingFolderDao
import com.morshues.morshuesandroid.data.db.entity.SyncingFolder

@Database(entities = [SyncingFolder::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun syncingFolderDao(): SyncingFolderDao
}
