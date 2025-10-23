package com.morshues.morshuesandroid.ui.permission

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.morshues.morshuesandroid.R
import com.morshues.morshuesandroid.utils.PermissionHelper

@Composable
fun MediaPermissionGrantScreen(
    currentMediaAccess: PermissionHelper.MediaAccess,
    onPermissionResult: (PermissionHelper.MediaAccess) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val access = PermissionHelper.getCurrentMediaAccess(context)
                onPermissionResult(access)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var suppressed by rememberSaveable { mutableStateOf(false) }
    val showConfirm = (currentMediaAccess == PermissionHelper.MediaAccess.LIMITED) && !suppressed

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { _ ->
            val access = PermissionHelper.getCurrentMediaAccess(context)
            onPermissionResult(access)
        }
    )

    Column(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            if (currentMediaAccess == PermissionHelper.MediaAccess.LIMITED) {
                suppressed = false
                return@Button
            }
            PermissionHelper.requestMediaAccess(permissionLauncher)
        }) {
            Text(stringResource(R.string.media_permission_grant_button))
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { suppressed = true },
            title = { Text(stringResource(R.string.media_permission_full_access_title)) },
            text = { Text(stringResource(R.string.media_permission_full_access_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null)
                        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                ) {
                    Text(stringResource(R.string.media_permission_full_access_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { suppressed = true }) {
                    Text(stringResource(R.string.media_permission_full_access_cancel))
                }
            }
        )
    }
}