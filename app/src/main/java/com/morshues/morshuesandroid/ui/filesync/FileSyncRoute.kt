package com.morshues.morshuesandroid.ui.filesync

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.morshues.morshuesandroid.ui.permission.MediaPermissionGate

@Composable
fun FileSyncRoute(
    navController: NavController,
) {
    MediaPermissionGate {
        val fileSyncViewModel: FileSyncViewModel = hiltViewModel()
        val uiState by fileSyncViewModel.uiState.collectAsState()
        FileSyncScreen(
            navController = navController,
            uiState = uiState,
            onFileItemSelected = fileSyncViewModel::onFileItemSelected,
            canBackward = fileSyncViewModel::canBackward,
            onBackward = fileSyncViewModel::backward,
            setSyncingFolder = fileSyncViewModel::setSyncingFolder,
            onErrorDismissed = fileSyncViewModel::clearErrorMessage,
        )
    }
}
