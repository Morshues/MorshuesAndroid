package com.morshues.morshuesandroid.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.morshues.morshuesandroid.di.AppModule

@Composable
fun SettingsRoute(
    navController: NavController
) {
    val factory = SettingsViewModelFactory(AppModule.settingsManager)
    val viewModel: SettingsViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()

    SettingsScreen(
        navController = navController,
        uiState = uiState,
        onErrorDismissed = viewModel::clearErrorMessage,
        onAction = viewModel::onAction,
    )
}
