package com.morshues.morshuesandroid.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.morshues.morshuesandroid.data.db.entity.SyncingFolder
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncingFolderDao {
    @Query("SELECT * FROM syncing_folders ORDER BY path ASC")
    fun getSyncingFolders(): Flow<List<SyncingFolder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: SyncingFolder)

    @Delete
    suspend fun deleteFolder(folder: SyncingFolder)
}