package com.morshues.morshuesandroid.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun SettingsRoute(
    navController: NavController
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    SettingsScreen(
        navController = navController,
        uiState = uiState,
        onErrorDismissed = viewModel::clearErrorMessage,
        onAction = viewModel::onAction,
    )
}
