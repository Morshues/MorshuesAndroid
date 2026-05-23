package com.morshues.morshuesandroid.ui.filesync

import android.content.IntentSender
import android.os.Build
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.morshues.morshuesandroid.data.db.entity.SyncingFolder
import com.morshues.morshuesandroid.data.model.FileItem
import com.morshues.morshuesandroid.data.model.FolderItem
import com.morshues.morshuesandroid.data.model.StorageItem
import com.morshues.morshuesandroid.data.model.toStorageItem
import com.morshues.morshuesandroid.data.repository.LocalFileRepository
import com.morshues.morshuesandroid.data.repository.RemoteFileRepository
import com.morshues.morshuesandroid.data.repository.SyncTaskRepository
import com.morshues.morshuesandroid.data.db.entity.SyncType
import com.morshues.morshuesandroid.data.repository.SyncingFolderRepository
import com.morshues.morshuesandroid.data.sync.SyncTaskEnqueuer
import com.morshues.morshuesandroid.data.worker.SyncProcessorWorker
import com.morshues.morshuesandroid.domain.usecase.SyncFolderUseCase
import com.morshues.morshuesandroid.settings.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import javax.inject.Inject

enum class FileSyncViewType { FILE_SYSTEM, SYNCING_ONLY }

const val SYNCING_ROOT_PATH = "__syncing_root__"

data class FileSyncUiState(
    val breadCrumbs: List<StorageItem> = emptyList(),
    val files: List<StorageItem> = emptyList(),
    val syncingFolders: List<SyncingFolder> = emptyList(),
    val currentFolderRemoteFilesSet: Set<String> = emptySet(),
    val isProcessing: Boolean = false,
    val syncInProgressCount: Int = 0,
    val syncPendingCount: Int = 0,
    val errorMessage: String? = null,
    val pendingLocalDeleteFile: FileItem? = null,
    val pendingDeleteIntentSender: IntentSender? = null,
    val sortAscending: Boolean = true,
    val viewType: FileSyncViewType = FileSyncViewType.FILE_SYSTEM,
) {
    val syncingFolderPaths: Set<String> = syncingFolders.map { it.path }.toSet()

    val currentFolder: StorageItem? = breadCrumbs.lastOrNull()

    val isCurrentFolderSyncing: Boolean = currentFolder?.path in syncingFolderPaths

    val isAtSyncingRoot: Boolean =
        viewType == FileSyncViewType.SYNCING_ONLY && breadCrumbs.size == 1

    val isSyncing
        get() = syncInProgressCount + syncPendingCount > 0
}

@HiltViewModel
class FileSyncViewModel @Inject constructor(
    private val remoteFileRepository: RemoteFileRepository,
    private val localFileRepository: LocalFileRepository,
    private val syncingFolderRepository: SyncingFolderRepository,
    private val syncTaskRepository: SyncTaskRepository,
    private val syncTaskEnqueuer: SyncTaskEnqueuer,
    private val syncFolderUseCase: SyncFolderUseCase,
    private val workManager: WorkManager,
    private val settingsManager: SettingsManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FileSyncUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val savedViewType = settingsManager.getFileSyncViewType().first()
            if (savedViewType == SettingsManager.FILE_SYNC_VIEW_TYPE_SYNCING_ONLY) {
                val syncingFolders = syncingFolderRepository.getSyncingFolders().first()
                val syncingRoot = FolderItem(name = "Syncing Folders", path = SYNCING_ROOT_PATH)
                _uiState.update {
                    it.copy(
                        viewType = FileSyncViewType.SYNCING_ONLY,
                        breadCrumbs = listOf(syncingRoot),
                        files = buildSyncingRootItems(syncingFolders),
                        syncingFolders = syncingFolders,
                    )
                }
            } else {
                val defaultRootDir = Environment.getExternalStorageDirectory().toStorageItem()
                listDeviceFiles(defaultRootDir)
            }
            startObservers()
        }
    }

    private fun startObservers() {
        syncingFolderRepository.getSyncingFolders()
            .flowOn(Dispatchers.IO)
            .distinctUntilChanged()
            .onEach { folders ->
                _uiState.update { state ->
                    val updated = state.copy(syncingFolders = folders)
                    if (updated.isAtSyncingRoot) {
                        updated.copy(files = buildSyncingRootItems(folders))
                    } else {
                        updated
                    }
                }
            }
            .launchIn(viewModelScope)

        syncTaskRepository.getActiveTaskCount()
            .distinctUntilChanged()
            .onEach { count ->
                _uiState.update { it.copy(syncInProgressCount = count) }
            }
            .launchIn(viewModelScope)

        syncTaskRepository.getPendingTaskCount()
            .distinctUntilChanged()
            .onEach { count ->
                _uiState.update { it.copy(syncPendingCount = count) }
            }
            .launchIn(viewModelScope)

        syncTaskRepository.getCompletedTasks()
            .distinctUntilChanged()
            .onEach { completedTasks ->
                if (_uiState.value.currentFolder?.path !in _uiState.value.syncingFolderPaths) {
                    return@onEach
                }

                val tasksForCurrentFolder = completedTasks
                    .filter { it.folderPath == _uiState.value.currentFolder?.path }
                if (tasksForCurrentFolder.isEmpty()) {
                    return@onEach
                }

                _uiState.update { state ->
                    val updatedRemoteFiles = state.currentFolderRemoteFilesSet.toMutableSet()
                    tasksForCurrentFolder
                        .filter { it.syncType == SyncType.UPLOAD }
                        .forEach { it -> updatedRemoteFiles.add(it.fileName) }
                    state.copy(currentFolderRemoteFilesSet = updatedRemoteFiles)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun listDeviceFiles(file: StorageItem) {
        val newBreadCrumbs = _uiState.value.breadCrumbs + file
        val newFiles = filterForViewType(
            localFileRepository.listFiles(file.path, _uiState.value.sortAscending)
        )
        _uiState.update { it.copy(breadCrumbs = newBreadCrumbs, files = newFiles) }
    }

    private fun filterForViewType(items: List<StorageItem>): List<StorageItem> {
        return if (_uiState.value.viewType == FileSyncViewType.SYNCING_ONLY) {
            items.filterIsInstance<FileItem>()
        } else {
            items
        }
    }

    fun onFileItemSelected(file: StorageItem) {
        if (file is FolderItem) {
            listDeviceFiles(file)
            checkRemoteSyncingFolder(file.path)
        }
    }

    fun canBackward(): Boolean {
        return _uiState.value.breadCrumbs.size > 1
    }

    fun backward(): Boolean {
        if (_uiState.value.breadCrumbs.size <= 1) {
            return false
        }
        val newBreadCrumbs = _uiState.value.breadCrumbs.dropLast(1)
        val isSyncingRoot = _uiState.value.viewType == FileSyncViewType.SYNCING_ONLY
            && newBreadCrumbs.size == 1
        val newFiles = if (isSyncingRoot) {
            buildSyncingRootItems(_uiState.value.syncingFolders)
        } else {
            filterForViewType(
                localFileRepository.listFiles(
                    newBreadCrumbs.last().path,
                    _uiState.value.sortAscending,
                )
            )
        }
        _uiState.update { it.copy(breadCrumbs = newBreadCrumbs, files = newFiles) }

        if (!isSyncingRoot) {
            _uiState.value.currentFolder?.path?.let {
                checkRemoteSyncingFolder(it)
            }
        }
        return true
    }

    fun toggleViewType() {
        val newType = if (_uiState.value.viewType == FileSyncViewType.FILE_SYSTEM) {
            FileSyncViewType.SYNCING_ONLY
        } else {
            FileSyncViewType.FILE_SYSTEM
        }
        if (newType == FileSyncViewType.SYNCING_ONLY) {
            val syncingFolders = _uiState.value.syncingFolders
            val syncingRoot = FolderItem(name = "Syncing Folders", path = SYNCING_ROOT_PATH)
            _uiState.update {
                it.copy(
                    viewType = newType,
                    breadCrumbs = listOf(syncingRoot),
                    files = buildSyncingRootItems(syncingFolders),
                    currentFolderRemoteFilesSet = emptySet(),
                )
            }
        } else {
            val defaultRootDir = Environment.getExternalStorageDirectory().toStorageItem()
            _uiState.update {
                it.copy(
                    viewType = newType,
                    breadCrumbs = emptyList(),
                    currentFolderRemoteFilesSet = emptySet(),
                )
            }
            listDeviceFiles(defaultRootDir)
        }
        viewModelScope.launch {
            settingsManager.setFileSyncViewType(
                if (newType == FileSyncViewType.SYNCING_ONLY) {
                    SettingsManager.FILE_SYNC_VIEW_TYPE_SYNCING_ONLY
                } else {
                    SettingsManager.FILE_SYNC_VIEW_TYPE_FILE_SYSTEM
                }
            )
        }
    }

    private fun buildSyncingRootItems(folders: List<SyncingFolder>): List<StorageItem> {
        return folders
            .map { FolderItem(name = it.path.substringAfterLast('/'), path = it.path) }
            .sortedBy { it.name }
    }

    fun setSyncingFolder(path: String, toSync: Boolean) {
        viewModelScope.launch {
            if (toSync) {
                syncingFolderRepository.addSyncingFolder(path)
                syncFolder(path)
            } else {
                syncTaskEnqueuer.cancelWorkersByFolder(path)
                syncingFolderRepository.deleteSyncingFolder(path)
                syncTaskRepository.deleteTasksByFolder(path)
            }
        }
    }

    fun toggleSortOrder() {
        val newAscending = !_uiState.value.sortAscending
        val currentPath = _uiState.value.currentFolder?.path
        val newFiles = if (_uiState.value.isAtSyncingRoot || currentPath == null) {
            _uiState.value.files
        } else {
            filterForViewType(localFileRepository.listFiles(currentPath, newAscending))
        }
        _uiState.update { it.copy(sortAscending = newAscending, files = newFiles) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun checkRemoteSyncingFolder(folderPath: String) {
        if (folderPath !in _uiState.value.syncingFolderPaths) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }
            try {
                val listFolderResult = remoteFileRepository.listFolder(folderPath)
                val remoteFiles = listFolderResult.entries
                    .map { it.name }
                    .toSet()
                _uiState.update {
                    it.copy(currentFolderRemoteFilesSet = remoteFiles, isProcessing = false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        currentFolderRemoteFilesSet = emptySet(),
                        isProcessing = false,
                        errorMessage = "Failed to check remote files: ${e.message}"
                    )
                }
            }
        }
    }

    private fun syncFolder(folderPath: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }

            val result = withContext(Dispatchers.IO) {
                syncFolderUseCase(folderPath)
            }

            result.onSuccess { tasksCreated ->
                if (tasksCreated > 0) {
                    triggerSyncProcessor()
                }
                _uiState.update { it.copy(isProcessing = false) }
            }.onFailure { e ->
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = "Failed to sync folder: ${e.message}"
                    )
                }
            }
        }
    }

    private fun triggerSyncProcessor() {
        val processorRequest = OneTimeWorkRequestBuilder<SyncProcessorWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInputData(
                workDataOf(SyncProcessorWorker.KEY_MAX_CONCURRENT to 3)
            )
            .build()

        workManager.enqueueUniqueWork(
            "manual_sync_processor",
            ExistingWorkPolicy.KEEP,
            processorRequest
        )
    }

    fun deleteFile(file: FileItem) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }

            val deleted = withContext(Dispatchers.IO) { localFileRepository.deleteFile(file.path) }
            if (deleted) {
                deleteFromServer(file)
                return@launch
            }

            // File is owned by another app — request user confirmation via system dialog
            val intentSender = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                withContext(Dispatchers.IO) { localFileRepository.getDeleteIntentSender(file.path) }
            } else null

            if (intentSender != null) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        pendingLocalDeleteFile = file,
                        pendingDeleteIntentSender = intentSender,
                    )
                }
            } else {
                _uiState.update {
                    it.copy(isProcessing = false, errorMessage = "Failed to delete local file")
                }
            }
        }
    }

    fun onLocalDeleteConfirmed() {
        val file = _uiState.value.pendingLocalDeleteFile ?: return
        _uiState.update { it.copy(pendingLocalDeleteFile = null, pendingDeleteIntentSender = null) }
        viewModelScope.launch { deleteFromServer(file) }
    }

    fun onLocalDeleteDismissed() {
        _uiState.update { it.copy(pendingLocalDeleteFile = null, pendingDeleteIntentSender = null) }
    }

    private suspend fun deleteFromServer(file: FileItem) {
        val currentFolder = _uiState.value.currentFolder ?: return
        _uiState.update { it.copy(isProcessing = true) }
        try {
            remoteFileRepository.deleteFile(currentFolder.path, file.name)
            val newFiles = filterForViewType(
                localFileRepository.listFiles(
                    currentFolder.path,
                    _uiState.value.sortAscending,
                )
            )
            _uiState.update { state ->
                val updatedRemoteFiles = state.currentFolderRemoteFilesSet.toMutableSet()
                updatedRemoteFiles.remove(file.name)
                state.copy(
                    files = newFiles,
                    currentFolderRemoteFilesSet = updatedRemoteFiles,
                    isProcessing = false,
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.update {
                it.copy(isProcessing = false, errorMessage = "Failed to delete from server: ${e.message}")
            }
        }
    }
}
