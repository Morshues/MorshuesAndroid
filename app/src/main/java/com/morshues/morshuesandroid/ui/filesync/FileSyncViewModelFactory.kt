package com.morshues.morshuesandroid.ui.filesync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.morshues.morshuesandroid.data.repository.RemoteFileRepository
import com.morshues.morshuesandroid.data.repository.LocalFileRepository
import com.morshues.morshuesandroid.data.repository.SyncTaskRepository
import com.morshues.morshuesandroid.data.repository.SyncingFolderRepository
import com.morshues.morshuesandroid.data.sync.SyncTaskEnqueuer
import com.morshues.morshuesandroid.domain.usecase.ProcessSyncQueueUseCase
import com.morshues.morshuesandroid.domain.usecase.SyncFolderUseCase

class FileSyncViewModelFactory(
    private val remoteFileRepository: RemoteFileRepository,
    private val localFileRepository: LocalFileRepository,
    private val syncingFolderRepository: SyncingFolderRepository,
    private val syncTaskRepository: SyncTaskRepository,
    private val syncTaskEnqueuer: SyncTaskEnqueuer,
    private val syncFolderUseCase: SyncFolderUseCase,
    private val processSyncQueueUseCase: ProcessSyncQueueUseCase,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FileSyncViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FileSyncViewModel(
                remoteFileRepository,
                localFileRepository,
                syncingFolderRepository,
                syncTaskRepository,
                syncTaskEnqueuer,
                syncFolderUseCase,
                processSyncQueueUseCase,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
