package com.morshues.morshuesandroid.ui.remotecontrol

import androidx.lifecycle.ViewModel
import com.morshues.morshuesandroid.data.websocket.WebSocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class RemoteControlPanelViewModel @Inject constructor(
    private val webSocketManager: WebSocketManager,
) : ViewModel() {

    data class UiState(
        val url: String = "",
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

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
        webSocketManager.send("link_page_navigate", buildJsonObject {
            put("instruction", "scroll")
            put("x", x * -100)
            put("y", y * -100)
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

    // {"action":"back"}
    fun sendBack() {
        webSocketManager.send("back")
    }

    // {"action":"home"}
    fun sendHome() {
        webSocketManager.send("home")
    }
}
