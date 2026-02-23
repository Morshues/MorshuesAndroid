package com.morshues.morshuesandroid.ui.remotecontrol

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.morshues.morshuesandroid.data.websocket.WebSocketManager
import com.morshues.morshuesandroid.settings.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RemoteControlViewModel @Inject constructor(
    private val webSocketManager: WebSocketManager,
    private val settingsManager: SettingsManager,
) : ViewModel() {

    data class UiState(
        val host: String = "",
        val port: String = "",
        val connectionStatus: WebSocketManager.ConnectionStatus = WebSocketManager.ConnectionStatus.Idle,
        val currentScreen: String? = null,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val host = settingsManager.getRemoteControlHost().first()
            val port = settingsManager.getRemoteControlPort().first()
            _uiState.update { it.copy(host = host, port = port) }
        }

        webSocketManager.connectionStatus
            .onEach { status ->
                _uiState.update { it.copy(connectionStatus = status) }
                if (status is WebSocketManager.ConnectionStatus.Connected) {
                    val host = _uiState.value.host
                    val port = _uiState.value.port
                    if (host.isNotBlank() && port.isNotBlank()) {
                        settingsManager.setRemoteControlHost(host)
                        settingsManager.setRemoteControlPort(port)
                    }
                }
            }
            .launchIn(viewModelScope)

        webSocketManager.currentScreen
            .onEach { screen -> _uiState.update { it.copy(currentScreen = screen) } }
            .launchIn(viewModelScope)
    }

    fun onHostChange(host: String) {
        _uiState.update { it.copy(host = host) }
    }

    fun onPortChange(port: String) {
        _uiState.update { it.copy(port = port) }
    }

    fun connect() {
        val host = _uiState.value.host.trim()
        val port = _uiState.value.port.trim()
        if (host.isBlank() || port.isBlank()) return
        webSocketManager.connect(host, port)
    }

    fun disconnect() {
        webSocketManager.disconnect()
    }
}
