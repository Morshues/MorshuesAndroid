package com.morshues.morshuesandroid.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.morshues.morshuesandroid.settings.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    // Future: Add more setting categories here
    // sealed interface Theme : SettingsAction { ... }
    // sealed interface Notifications : SettingsAction { ... }
}

data class SettingsUiState(
    val currentServerPath: String = "",
    val rootUrlList: List<String> = emptyList(),
    val errorMessage: String? = null,
)

class SettingsViewModel(
    private val settingsManager: SettingsManager
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
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.ServerUrl.Select -> selectServerPath(action.url)
            is SettingsAction.ServerUrl.Add -> addUrl(action.url)
            is SettingsAction.ServerUrl.Delete -> deleteUrl(action.url)
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
}
