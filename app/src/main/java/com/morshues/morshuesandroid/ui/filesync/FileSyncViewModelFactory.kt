package com.morshues.morshuesandroid.ui.filesync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkManager
import com.morshues.morshuesandroid.data.repository.RemoteFileRepository
import com.morshues.morshuesandroid.data.repository.LocalFileRepository

import com.morshues.morshuesandroid.data.repository.SyncingFolderRepository

class FileSyncViewModelFactory(
    private val remoteFileRepository: RemoteFileRepository,
    private val localFileRepository: LocalFileRepository,
    private val syncingFolderRepository: SyncingFolderRepository,
    private val workManager: WorkManager,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FileSyncViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FileSyncViewModel(
                remoteFileRepository,
                localFileRepository,
                syncingFolderRepository,
                workManager,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
