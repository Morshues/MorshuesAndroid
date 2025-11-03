package com.morshues.morshuesandroid.data.repository

import com.morshues.morshuesandroid.data.db.dao.SyncingFolderDao
import com.morshues.morshuesandroid.data.db.entity.SyncingFolder
import kotlinx.coroutines.flow.Flow

class SyncingFolderRepository(private val syncingFolderDao: SyncingFolderDao) {
    fun getSyncingFolders(): Flow<List<SyncingFolder>> {
        return syncingFolderDao.getSyncingFolders()
    }

    suspend fun addSyncingFolder(path: String) {
        val folder = SyncingFolder(path = path)
        syncingFolderDao.insertFolder(folder)
    }

    suspend fun deleteSyncingFolder(path: String) {
        val folder = SyncingFolder(path = path)
        syncingFolderDao.deleteFolder(folder)
    }
}
