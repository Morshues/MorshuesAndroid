package com.morshues.morshuesandroid.ui.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.morshues.morshuesandroid.utils.PermissionHelper

@Composable
fun MediaPermissionGate(
    content: @Composable () -> Unit,
) {
    val permissionGrantViewModel: MediaPermissionGrantViewModel = viewModel()
    val currentMediaAccess by permissionGrantViewModel.currentMediaAccess.collectAsState()
    if (currentMediaAccess != PermissionHelper.MediaAccess.FULL_MEDIA
        && currentMediaAccess != PermissionHelper.MediaAccess.FULL_FILES
    ) {
        MediaPermissionGrantScreen(
            currentMediaAccess = currentMediaAccess,
            onPermissionResult = permissionGrantViewModel::onPermissionResult,
        )
        return
    }

    content()
}