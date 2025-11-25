package com.morshues.morshuesandroid.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.morshues.morshuesandroid.data.db.entity.SyncType
import com.morshues.morshuesandroid.data.repository.SyncTaskRepository
import com.morshues.morshuesandroid.data.repository.SyncingFolderRepository
import com.morshues.morshuesandroid.data.sync.SyncTaskEnqueuer
import com.morshues.morshuesandroid.domain.usecase.SyncFolderUseCase
import com.morshues.morshuesandroid.settings.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface SettingsAction {
    sealed interface ServerUrl : SettingsAction {
        data class Select(val url: String) : ServerUrl
        data class Add(val url: String) : ServerUrl
        data class Delete(val url: String) : ServerUrl
    }

    sealed interface SyncNetwork : SettingsAction {
        data class SetNetworkType(val networkType: String) : SyncNetwork
    }

    sealed interface SyncMode : SettingsAction {
        data class SetMode(val mode: String) : SyncMode
    }

    // Future: Add more setting categories here
    // sealed interface Theme : SettingsAction { ... }
    // sealed interface Notifications : SettingsAction { ... }
}

data class SettingsUiState(
    val currentServerPath: String = "",
    val rootUrlList: List<String> = emptyList(),
    val syncNetworkType: String = SettingsManager.DEFAULT_SYNC_NETWORK_TYPE,
    val syncMode: String = SettingsManager.DEFAULT_SYNC_MODE,
    val errorMessage: String? = null,
)

class SettingsViewModel(
    private val settingsManager: SettingsManager,
    private val syncTaskRepository: SyncTaskRepository,
    private val syncingFolderRepository: SyncingFolderRepository,
    private val syncTaskEnqueuer: SyncTaskEnqueuer,
    private val syncFolderUseCase: SyncFolderUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        settingsManager.getServerPath()
            .onEach { serverPath ->
                _uiState.update { it.copy(currentServerPath = serverPath) }
            }
            .launchIn(viewModelScope)

        settingsManager.getRootUrlList()
            .onEach { urlList ->
                _uiState.update { it.copy(rootUrlList = urlList) }
            }
            .launchIn(viewModelScope)

        settingsManager.getSyncNetworkType()
            .onEach { networkType ->
                _uiState.update { it.copy(syncNetworkType = networkType) }
            }
            .launchIn(viewModelScope)

        settingsManager.getSyncMode()
            .onEach { syncMode ->
                _uiState.update { it.copy(syncMode = syncMode) }
            }
            .launchIn(viewModelScope)
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.ServerUrl.Select -> selectServerPath(action.url)
            is SettingsAction.ServerUrl.Add -> addUrl(action.url)
            is SettingsAction.ServerUrl.Delete -> deleteUrl(action.url)
            is SettingsAction.SyncNetwork.SetNetworkType -> setSyncNetworkType(action.networkType)
            is SettingsAction.SyncMode.SetMode -> setSyncMode(action.mode)
        }
    }

    private fun selectServerPath(url: String) {
        viewModelScope.launch {
            settingsManager.setServerPath(url)
        }
    }

    private fun addUrl(url: String) {
        val trimmedUrl = url.trim()
        val toSaveUrl = if (trimmedUrl.endsWith('/')) trimmedUrl else "$trimmedUrl/"

        viewModelScope.launch {
            if (_uiState.value.rootUrlList.contains(toSaveUrl)) {
                _uiState.update { it.copy(errorMessage = "This URL already exists") }
                return@launch
            }

            if (!toSaveUrl.startsWith("http://") && !toSaveUrl.startsWith("https://")) {
                _uiState.update { it.copy(errorMessage = "The URL must start with http:// or https://") }
                return@launch
            }

            settingsManager.addRootUrl(toSaveUrl)
        }
    }

    private fun deleteUrl(url: String) {
        viewModelScope.launch {
            if (_uiState.value.rootUrlList.size <= 1) {
                _uiState.update { it.copy(errorMessage = "Need at least one root URL") }
                return@launch
            }

            settingsManager.removeRootUrl(url)
        }
    }

    private fun setSyncNetworkType(networkType: String) {
        viewModelScope.launch {
            settingsManager.setSyncNetworkType(networkType)
        }
    }

    private fun setSyncMode(mode: String) {
        viewModelScope.launch {
            settingsManager.setSyncMode(mode)

            when (mode) {
                SettingsManager.SYNC_MODE_DISABLED -> {
                    syncTaskEnqueuer.cancelAllUploadWorkers()
                    syncTaskEnqueuer.cancelAllDownloadWorkers()
                    syncTaskRepository.deleteTasksBySyncType(SyncType.DOWNLOAD)
                    syncTaskRepository.deleteTasksBySyncType(SyncType.UPLOAD)
                }
                SettingsManager.SYNC_MODE_DOWNLOAD_ONLY -> {
                    syncTaskEnqueuer.cancelAllUploadWorkers()
                    syncTaskRepository.deleteTasksBySyncType(SyncType.UPLOAD)
                }
                SettingsManager.SYNC_MODE_UPLOAD_ONLY -> {
                    syncTaskEnqueuer.cancelAllDownloadWorkers()
                    syncTaskRepository.deleteTasksBySyncType(SyncType.DOWNLOAD)
                }
                SettingsManager.SYNC_MODE_FULL -> {
                    // Keep all tasks, no deletion needed
                }
            }

            if (mode != SettingsManager.SYNC_MODE_DISABLED) {
                val syncingFolders = syncingFolderRepository.getSyncingFolders().first()
                syncingFolders.forEach { folder ->
                    syncFolderUseCase(folder.path)
                }
            }
        }
    }
}
