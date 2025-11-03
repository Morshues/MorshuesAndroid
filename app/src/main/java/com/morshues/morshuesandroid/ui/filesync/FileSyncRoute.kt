package com.morshues.morshuesandroid.ui.filesync

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.morshues.morshuesandroid.di.AppModule
import com.morshues.morshuesandroid.ui.permission.MediaPermissionGate

@Composable
fun FileSyncRoute(
    navController: NavController,
) {
    MediaPermissionGate {
        val factory = FileSyncViewModelFactory(
            AppModule.remoteFileRepository,
            AppModule.localFileRepository,
            AppModule.syncingFolderRepository,
            AppModule.workManager,
        )
        val fileSyncViewModel: FileSyncViewModel = viewModel(factory = factory)
        val uiState by fileSyncViewModel.uiState.collectAsState()
        FileSyncScreen(
            navController = navController,
            uiState = uiState,
            onFileItemSelected = fileSyncViewModel::onFileItemSelected,
            onBackward = fileSyncViewModel::backward,
            setSyncingFolder = fileSyncViewModel::setSyncingFolder,
            onErrorDismissed = fileSyncViewModel::clearErrorMessage,
        )
    }
}
