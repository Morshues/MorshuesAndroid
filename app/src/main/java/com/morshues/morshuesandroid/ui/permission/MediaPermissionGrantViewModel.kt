package com.morshues.morshuesandroid.ui.permission

import androidx.lifecycle.ViewModel
import com.morshues.morshuesandroid.util.PermissionHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MediaPermissionGrantViewModel : ViewModel() {
    private val _currentMediaAccess = MutableStateFlow(PermissionHelper.MediaAccess.NONE)
    val currentMediaAccess: StateFlow<PermissionHelper.MediaAccess> = _currentMediaAccess

    fun onPermissionResult(mediaAccess: PermissionHelper.MediaAccess) {
        // To avoid currentMediaAccess continuous changed event
        if (currentMediaAccess.value != mediaAccess) {
            _currentMediaAccess.value = mediaAccess
        }
    }
}