package com.morshues.morshuesandroid.ui.remotecontrol

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun RemoteControlRoute(navController: NavController) {
    val viewModel: RemoteControlViewModel = hiltViewModel()
    val panelViewModel: RemoteControlPanelViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val panelUiState by panelViewModel.uiState.collectAsState()

    RemoteControlScreen(
        navController = navController,
        uiState = uiState,
        panelUiState = panelUiState,
        connectionActions = ConnectionActions(
            onHostChange = viewModel::onHostChange,
            onPortChange = viewModel::onPortChange,
            onConnect = viewModel::connect,
        ),
        baseActions = PanelBaseActions(
            onDisconnect = viewModel::disconnect,
            onBack = panelViewModel::sendBack,
            onHome = panelViewModel::sendHome,
        ),
        mainPageActions = MainPageActions(
            onUrlChange = panelViewModel::onUrlChange,
            onSendUrl = panelViewModel::sendUrl,
            onNavigate = panelViewModel::sendMainNavigate,
        ),
        linkPageActions = LinkPageActions(
            onJoystickMove = panelViewModel::sendScroll,
            onZoom = panelViewModel::sendZoom,
            onBgInv = panelViewModel::sendBgInv,
        ),
    )
}
