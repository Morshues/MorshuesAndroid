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
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class RemoteControlPanelViewModel @Inject constructor(
    private val webSocketManager: WebSocketManager,
    private val settingsManager: SettingsManager,
) : ViewModel() {

    data class UiState(
        val url: String = "",
        val videoState: WebSocketManager.VideoState? = null,
        val seekSeconds: Int = SettingsManager.DEFAULT_REMOTE_CONTROL_SEEK_SECONDS,
        val scrollScale: Int = SettingsManager.DEFAULT_REMOTE_CONTROL_SCROLL_SCALE,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val seconds = settingsManager.getRemoteControlSeekSeconds().first()
            val scrollScale = settingsManager.getRemoteControlScrollScale().first()
            _uiState.update { it.copy(seekSeconds = seconds, scrollScale = scrollScale) }
        }

        webSocketManager.videoState
            .onEach { state -> _uiState.update { it.copy(videoState = state) } }
            .launchIn(viewModelScope)
    }

    fun onSeekSecondsChange(seconds: Int) {
        if (seconds == _uiState.value.seekSeconds) return
        _uiState.update { it.copy(seekSeconds = seconds) }
        viewModelScope.launch { settingsManager.setRemoteControlSeekSeconds(seconds) }
    }

    fun onScrollScaleChange(scale: Int) {
        if (scale == _uiState.value.scrollScale) return
        _uiState.update { it.copy(scrollScale = scale) }
        viewModelScope.launch { settingsManager.setRemoteControlScrollScale(scale) }
    }

    fun onUrlChange(url: String) {
        _uiState.update { it.copy(url = url) }
    }

    // {"action":"open_url","data":{"url":"..."}}
    fun sendUrl() {
        val url = _uiState.value.url.trim()
        if (url.isBlank()) return
        webSocketManager.send("open_url", buildJsonObject { put("url", url) })
    }

    // {"action":"main_navigate","data":{"d":"up|down|left|right|ok"}}
    fun sendMainNavigate(direction: DPadDirection) {
        val d = when (direction) {
            DPadDirection.UP    -> "up"
            DPadDirection.DOWN  -> "down"
            DPadDirection.LEFT  -> "left"
            DPadDirection.RIGHT -> "right"
            DPadDirection.OK    -> "ok"
        }
        webSocketManager.send("main_navigate", buildJsonObject { put("d", d) })
    }

    // {"action":"link_page_navigate","data":{"instruction":"scroll","direction":"up|down|left|right","delta":N}}
    fun sendScroll(x: Float, y: Float) {
        if (abs(x) < 0.05f && abs(y) < 0.05f) return
        val scale = _uiState.value.scrollScale
        webSocketManager.send("link_page_navigate", buildJsonObject {
            put("instruction", "scroll")
            put("x", x * -scale)
            put("y", y * -scale)
        })
    }

    // {"action":"link_page_navigate","data":{"instruction":"zoom","delta":0.5 or -0.5}}
    fun sendZoom(zoomIn: Boolean) {
        webSocketManager.send("link_page_navigate", buildJsonObject {
            put("instruction", "zoom")
            put("delta", if (zoomIn) 0.5 else -0.5)
        })
    }

    // {"action":"link_page_navigate","data":{"instruction":"bg_inv"}}
    fun sendBgInv() {
        webSocketManager.send("link_page_navigate", buildJsonObject { put("instruction", "bg_inv") })
    }

    // {"action":"video_control","data":{"instruction":"play_pause"}}
    fun sendVideoPlayPause() = sendVideoControl("play_pause")

    // {"action":"video_control","data":{"instruction":"seek","ms":N}}
    fun sendVideoSeek(deltaMs: Long) = sendVideoControl("seek") { put("ms", deltaMs) }

    // {"action":"video_control","data":{"instruction":"seek_to","ms":N}}
    fun sendVideoSeekTo(positionMs: Long) = sendVideoControl("seek_to") { put("ms", positionMs) }

    // {"action":"video_control","data":{"instruction":"next"}}
    fun sendVideoNext() = sendVideoControl("next")

    // {"action":"video_control","data":{"instruction":"previous"}}
    fun sendVideoPrevious() = sendVideoControl("previous")

    // {"action":"video_control","data":{"instruction":"speed","value":1.5}}
    fun sendVideoSpeed(value: Float) = sendVideoControl("speed") { put("value", value) }

    // {"action":"video_control","data":{"instruction":"get_state"}}
    fun requestVideoState() = sendVideoControl("get_state")

    private fun sendVideoControl(instruction: String, extra: JsonObjectBuilder.() -> Unit = {}) {
        webSocketManager.send("video_control", buildJsonObject {
            put("instruction", instruction)
            extra()
        })
    }

    // {"action":"back"}
    fun sendBack() {
        webSocketManager.send("back")
    }

    // {"action":"home"}
    fun sendHome() {
        webSocketManager.send("home")
    }
}
