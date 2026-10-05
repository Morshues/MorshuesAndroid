package com.morshues.morshuesandroid.ui.remotecontrol

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.morshues.morshuesandroid.R
import com.morshues.morshuesandroid.data.websocket.WebSocketManager.ConnectionStatus
import com.morshues.morshuesandroid.data.websocket.WebSocketManager.VideoState
import com.morshues.morshuesandroid.ui.components.CommonTopBar
import com.morshues.morshuesandroid.ui.theme.MainAndroidTheme

data class ConnectionActions(
    val onHostChange: (String) -> Unit,
    val onPortChange: (String) -> Unit,
    val onConnect: () -> Unit,
)

data class PanelBaseActions(
    val onDisconnect: () -> Unit,
    val onBack: () -> Unit,
    val onHome: () -> Unit,
)

@Composable
fun RemoteControlScreen(
    navController: NavController,
    uiState: RemoteControlViewModel.UiState,
    panelUiState: RemoteControlPanelViewModel.UiState,
    connectionActions: ConnectionActions,
    baseActions: PanelBaseActions,
    mainPageActions: MainPageActions,
    linkPageActions: LinkPageActions,
    videoActions: VideoActions,
) {
    Scaffold(
        topBar = {
            CommonTopBar(
                navController = navController,
                title = "Remote Control",
                showSettingsButton = false,
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState.connectionStatus) {
                is ConnectionStatus.Idle, is ConnectionStatus.Error, is ConnectionStatus.Connecting -> {
                    WebSocketConnectScreen(
                        host = uiState.host,
                        port = uiState.port,
                        error = (uiState.connectionStatus as? ConnectionStatus.Error)?.message,
                        isConnecting = uiState.connectionStatus is ConnectionStatus.Connecting,
                        onHostChange = connectionActions.onHostChange,
                        onPortChange = connectionActions.onPortChange,
                        onConnect = connectionActions.onConnect,
                    )
                }
                is ConnectionStatus.Connected -> {
                    RemoteControlPanel(
                        currentScreen = uiState.currentScreen,
                        url = panelUiState.url,
                        videoState = panelUiState.videoState,
                        seekSeconds = panelUiState.seekSeconds,
                        scrollScale = panelUiState.scrollScale,
                        baseActions = baseActions,
                        mainPageActions = mainPageActions,
                        linkPageActions = linkPageActions,
                        videoActions = videoActions,
                    )
                }
            }
        }
    }
}

@Composable
private fun RemoteControlPanel(
    currentScreen: String?,
    url: String,
    videoState: VideoState?,
    seekSeconds: Int,
    scrollScale: Int,
    baseActions: PanelBaseActions,
    mainPageActions: MainPageActions,
    linkPageActions: LinkPageActions,
    videoActions: VideoActions,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp, 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Connection status row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = baseActions.onBack,
                modifier = Modifier.size(32.dp),
                contentPadding = PaddingValues(0.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.round_arrow_back_24),
                    contentDescription = "Back",
                    modifier = Modifier.size(22.dp),
                )
            }

            Button(
                onClick = baseActions.onHome,
                modifier = Modifier.size(32.dp),
                contentPadding = PaddingValues(0.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.round_home_24),
                    contentDescription = "Home",
                    modifier = Modifier.size(22.dp),
                )
            }

            Spacer(Modifier.weight(1f))

            Text(
                text = currentScreen ?: "Unknown Panel",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge,
            )

            Spacer(Modifier.weight(1f))

            Spacer(Modifier.padding(18.dp))

            Button(
                onClick = baseActions.onDisconnect,
                modifier = Modifier.size(32.dp),
                contentPadding = PaddingValues(0.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.round_exit_to_app_24),
                    contentDescription = "Disconnect",
                    modifier = Modifier.size(22.dp),
                )
            }
        }

        Spacer(Modifier.weight(.3f))

        when (currentScreen) {
            "LinkPageActivity" -> LinkPageScreen(
                scrollScale = scrollScale,
                actions = linkPageActions,
            )
            "VideoPlayerActivity" -> VideoScreen(
                videoState = videoState,
                seekSeconds = seekSeconds,
                actions = videoActions,
            )
            else -> MainPageScreen(
                url = url,
                actions = mainPageActions,
            )
        }

        Spacer(Modifier.weight(1f))
    }
}

@Preview(showBackground = true, name = "Connect - Light")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Connect - Dark")
@Composable
fun RemoteControlConnectPreview() {
    MainAndroidTheme {
        RemoteControlScreen(
            navController = rememberNavController(),
            uiState = RemoteControlViewModel.UiState(connectionStatus = ConnectionStatus.Idle),
            panelUiState = RemoteControlPanelViewModel.UiState(),
            connectionActions = ConnectionActions({}, {}, {}),
            baseActions = PanelBaseActions({}, {}, {}),
            mainPageActions = MainPageActions({}, {}, {}),
            linkPageActions = LinkPageActions({ _, _ -> }, {}, {}, {}),
            videoActions = VideoActions({}, {}, {}, {}, {}, {}, {}, {}),
        )
    }
}

@Preview(showBackground = true, name = "Connecting")
@Composable
fun RemoteControlConnectingPreview() {
    MainAndroidTheme {
        RemoteControlScreen(
            navController = rememberNavController(),
            uiState = RemoteControlViewModel.UiState(
                host = "192.168.1.100", port = "8765",
                connectionStatus = ConnectionStatus.Connecting,
            ),
            panelUiState = RemoteControlPanelViewModel.UiState(),
            connectionActions = ConnectionActions({}, {}, {}),
            baseActions = PanelBaseActions({}, {}, {}),
            mainPageActions = MainPageActions({}, {}, {}),
            linkPageActions = LinkPageActions({ _, _ -> }, {}, {}, {}),
            videoActions = VideoActions({}, {}, {}, {}, {}, {}, {}, {}),
        )
    }
}

@Preview(showBackground = true, name = "Panel - MainPageScreen")
@Composable
fun RemoteControlMainPagePreview() {
    MainAndroidTheme {
        RemoteControlScreen(
            navController = rememberNavController(),
            uiState = RemoteControlViewModel.UiState(
                host = "192.168.1.100", port = "8765",
                connectionStatus = ConnectionStatus.Connected,
                currentScreen = "MainActivity",
            ),
            panelUiState = RemoteControlPanelViewModel.UiState(url = "https://example.com"),
            connectionActions = ConnectionActions({}, {}, {}),
            baseActions = PanelBaseActions({}, {}, {}),
            mainPageActions = MainPageActions({}, {}, {}),
            linkPageActions = LinkPageActions({ _, _ -> }, {}, {}, {}),
            videoActions = VideoActions({}, {}, {}, {}, {}, {}, {}, {}),
        )
    }
}

@Preview(showBackground = true, name = "Panel - LinkPageActivity")
@Composable
fun RemoteControlLinkPagePreview() {
    MainAndroidTheme {
        RemoteControlScreen(
            navController = rememberNavController(),
            uiState = RemoteControlViewModel.UiState(
                host = "192.168.1.100", port = "8765",
                connectionStatus = ConnectionStatus.Connected,
                currentScreen = "LinkPageActivity",
            ),
            panelUiState = RemoteControlPanelViewModel.UiState(),
            connectionActions = ConnectionActions({}, {}, {}),
            baseActions = PanelBaseActions({}, {}, {}),
            mainPageActions = MainPageActions({}, {}, {}),
            linkPageActions = LinkPageActions({ _, _ -> }, {}, {}, {}),
            videoActions = VideoActions({}, {}, {}, {}, {}, {}, {}, {}),
        )
    }
}

@Preview(showBackground = true, name = "Panel - VideoPlayerActivity")
@Composable
fun RemoteControlVideoPreview() {
    MainAndroidTheme {
        RemoteControlScreen(
            navController = rememberNavController(),
            uiState = RemoteControlViewModel.UiState(
                host = "192.168.1.100", port = "8765",
                connectionStatus = ConnectionStatus.Connected,
                currentScreen = "VideoPlayerActivity",
            ),
            panelUiState = RemoteControlPanelViewModel.UiState(
                videoState = VideoState(
                    title = "Sample Video", index = 0, count = 3,
                    positionMs = 83_000, durationMs = 1_520_000, isPlaying = true,
                ),
            ),
            connectionActions = ConnectionActions({}, {}, {}),
            baseActions = PanelBaseActions({}, {}, {}),
            mainPageActions = MainPageActions({}, {}, {}),
            linkPageActions = LinkPageActions({ _, _ -> }, {}, {}, {}),
            videoActions = VideoActions({}, {}, {}, {}, {}, {}, {}, {}),
        )
    }
}

@Preview(showBackground = true, name = "Error State")
@Composable
fun RemoteControlErrorPreview() {
    MainAndroidTheme {
        RemoteControlScreen(
            navController = rememberNavController(),
            uiState = RemoteControlViewModel.UiState(
                host = "192.168.1.100", port = "8765",
                connectionStatus = ConnectionStatus.Error("Connection refused"),
            ),
            panelUiState = RemoteControlPanelViewModel.UiState(),
            connectionActions = ConnectionActions({}, {}, {}),
            baseActions = PanelBaseActions({}, {}, {}),
            mainPageActions = MainPageActions({}, {}, {}),
            linkPageActions = LinkPageActions({ _, _ -> }, {}, {}, {}),
            videoActions = VideoActions({}, {}, {}, {}, {}, {}, {}, {}),
        )
    }
}
