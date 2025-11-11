package com.morshues.morshuesandroid.ui.filesync

import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.morshues.morshuesandroid.data.model.StorageItem
import com.morshues.morshuesandroid.data.repository.RemoteFileRepository
import com.morshues.morshuesandroid.data.repository.LocalFileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

import com.morshues.morshuesandroid.data.db.entity.SyncingFolder
import com.morshues.morshuesandroid.data.model.FileEntry
import com.morshues.morshuesandroid.data.model.FileItem
import com.morshues.morshuesandroid.data.model.FolderItem
import com.morshues.morshuesandroid.data.model.toStorageItem
import com.morshues.morshuesandroid.data.repository.SyncingFolderRepository
import com.morshues.morshuesandroid.data.worker.FileDownloadWorker
import com.morshues.morshuesandroid.data.worker.FileUploadWorker
import com.morshues.morshuesandroid.utils.MediaFileHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File

data class FileSyncUiState(
    val breadCrumbs: List<StorageItem> = emptyList(),
    val files: List<StorageItem> = emptyList(),
    val syncingFolders: List<SyncingFolder> = emptyList(),
    val currentFolderRemoteFilesSet: Set<String> = emptySet(),
    val isProcessing: Boolean = false,
    val errorMessage: String? = null,
) {
    val syncingFolderPaths: Set<String> = syncingFolders.map { it.path }.toSet()

    val currentFolder: StorageItem? = breadCrumbs.lastOrNull()

    val isCurrentFolderSyncing: Boolean = currentFolder?.path in syncingFolderPaths
}

class FileSyncViewModel(
    private val remoteFileRepository: RemoteFileRepository,
    private val localFileRepository: LocalFileRepository,
    private val syncingFolderRepository: SyncingFolderRepository,
    private val workManager: WorkManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FileSyncUiState())
    val uiState = _uiState.asStateFlow()

    init {
        val defaultRootDir = Environment.getExternalStorageDirectory().toStorageItem()
        listDeviceFiles(defaultRootDir)

        syncingFolderRepository.getSyncingFolders()
            .flowOn(Dispatchers.IO)
            .distinctUntilChanged()
            .onEach { folders ->
                _uiState.update { it.copy(syncingFolders = folders) }
            }
            .launchIn(viewModelScope)
    }

    private fun listDeviceFiles(file: StorageItem) {
        val newBreadCrumbs = _uiState.value.breadCrumbs + file
        val newFiles = localFileRepository.listFiles(file.path)
        _uiState.update { it.copy(breadCrumbs = newBreadCrumbs, files = newFiles) }
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
        val newFiles = localFileRepository.listFiles(newBreadCrumbs.last().path)
        _uiState.update { it.copy(breadCrumbs = newBreadCrumbs, files = newFiles) }

        _uiState.value.currentFolder?.path?.let {
            checkRemoteSyncingFolder(it)
        }
        return true
    }

    fun setSyncingFolder(path: String, toSync: Boolean) {
        viewModelScope.launch {
            if (toSync) {
                syncingFolderRepository.addSyncingFolder(path)
                syncFolder(path)
            } else {
                syncingFolderRepository.deleteSyncingFolder(path)
            }
        }
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
        val files = localFileRepository.listFiles(folderPath)
            .filterIsInstance<FileItem>()

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }
            try {
                val remoteCompareResult = withContext(Dispatchers.IO) {
                    remoteFileRepository.compareFolder(
                        folderPath = folderPath,
                        entries = files.map {
                            FileEntry(
                                name = it.name,
                                modifiedAt = it.lastModified,
                                size = it.sizeBytes,
                            )
                        }
                    )
                }

                remoteCompareResult.download
                    .filter { MediaFileHelper.isMediaFile(it.name) }
                    .forEach { fileEntry ->
                        enqueueFileDownload(folderPath, fileEntry.name)
                    }

                remoteCompareResult.upload.forEach { fileEntry ->
                    enqueueFileUpload(folderPath, fileEntry.name)
                }

                _uiState.update { it.copy(isProcessing = false) }
            } catch (e: Exception) {
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

    private fun enqueueFileUpload(folderPath: String, fileName: String) {
        val file = File(folderPath, fileName)
        val uploadData = workDataOf(
            FileUploadWorker.KEY_FOLDER_PATH to folderPath,
            FileUploadWorker.KEY_FILE_PATH to file.path
        )

        val uploadRequest = OneTimeWorkRequestBuilder<FileUploadWorker>()
            .setInputData(uploadData)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .addTag("upload_$folderPath")
            .build()

        workManager.enqueue(uploadRequest)
    }

    private fun enqueueFileDownload(folderPath: String, fileName: String) {
        val downloadData = workDataOf(
            FileDownloadWorker.KEY_FOLDER_PATH to folderPath,
            FileDownloadWorker.KEY_FILE_NAME to fileName,
        )

        val downloadRequest = OneTimeWorkRequestBuilder<FileDownloadWorker>()
            .setInputData(downloadData)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .addTag("download_$folderPath")
            .build()

        workManager.enqueue(downloadRequest)
    }
}
