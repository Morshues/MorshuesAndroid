package com.morshues.morshuesandroid.data.websocket

import android.os.SystemClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketManager @Inject constructor() {

    sealed class ConnectionStatus {
        object Idle : ConnectionStatus()
        object Connecting : ConnectionStatus()
        object Connected : ConnectionStatus()
        data class Error(val message: String) : ConnectionStatus()
    }

    data class VideoState(
        val title: String = "",
        val index: Int = 0,
        val count: Int = 0,
        val positionMs: Long = 0,
        val durationMs: Long = 0,
        val isPlaying: Boolean = false,
        val speed: Float = 1f,
        val receivedAt: Long = 0,
    )

    private val _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Idle)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _currentScreen = MutableStateFlow<String?>(null)
    val currentScreen: StateFlow<String?> = _currentScreen.asStateFlow()

    private val _videoState = MutableStateFlow<VideoState?>(null)
    val videoState: StateFlow<VideoState?> = _videoState.asStateFlow()

    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    fun connect(host: String, port: String) {
        _connectionStatus.update { ConnectionStatus.Connecting }

        val request = try {
            Request.Builder()
                .url("ws://$host:$port")
                .build()
        } catch (e: Exception) {
            _connectionStatus.update {
                ConnectionStatus.Error(e.message ?: "Incorrect host:port")
            }
            return
        }

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _connectionStatus.update { ConnectionStatus.Connected }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = Json.parseToJsonElement(text).jsonObject
                    when (json["action"]?.jsonPrimitive?.contentOrNull) {
                        "current_screen" -> {
                            val name = json["data"]?.jsonObject
                                ?.get("name")?.jsonPrimitive?.contentOrNull
                            _currentScreen.update { name }
                        }
                        "video_state" -> {
                            val data = json["data"]?.jsonObject ?: return
                            fun field(key: String) = data[key]?.jsonPrimitive?.contentOrNull
                            _videoState.update {
                                VideoState(
                                    title = field("title") ?: "",
                                    index = field("index")?.toIntOrNull() ?: 0,
                                    count = field("count")?.toIntOrNull() ?: 0,
                                    positionMs = field("position")?.toLongOrNull() ?: 0,
                                    durationMs = field("duration")?.toLongOrNull() ?: 0,
                                    isPlaying = field("isPlaying")?.toBooleanStrictOrNull() ?: false,
                                    speed = field("speed")?.toFloatOrNull() ?: 1f,
                                    receivedAt = SystemClock.elapsedRealtime(),
                                )
                            }
                        }
                    }
                } catch (_: Exception) {
                    // Ignore malformed messages
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _connectionStatus.update {
                    ConnectionStatus.Error(t.message ?: "Connection failed")
                }
                _currentScreen.update { null }
                _videoState.update { null }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _connectionStatus.update { ConnectionStatus.Idle }
                _currentScreen.update { null }
                _videoState.update { null }
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "User disconnected")
        webSocket = null
        _connectionStatus.update { ConnectionStatus.Idle }
        _currentScreen.update { null }
        _videoState.update { null }
    }

    fun send(action: String, data: JsonObject? = null): Boolean {
        val message = buildJsonObject {
            put("action", action)
            if (data != null) put("data", data)
        }.toString()
        return webSocket?.send(message) ?: false
    }
}
