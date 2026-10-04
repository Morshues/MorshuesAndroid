package com.morshues.morshuesandroid.ui.remotecontrol

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.morshues.morshuesandroid.ui.theme.MainAndroidTheme

@Composable
fun WebSocketConnectScreen(
    host: String,
    port: String,
    error: String?,
    isConnecting: Boolean,
    onHostChange: (String) -> Unit,
    onPortChange: (String) -> Unit,
    onConnect: () -> Unit,
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    var permissionDenied by remember { mutableStateOf(false) }
    // Once denied too many times the system stops showing the dialog; only app settings can grant it
    var permissionPermanentlyDenied by remember { mutableStateOf(false) }

    val openAppSettings = {
        context.startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.packageName, null),
            )
        )
    }

    // Android 17+ blocks LAN connections unless ACCESS_LOCAL_NETWORK is granted
    val localNetworkPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionDenied = !granted
        permissionPermanentlyDenied = !granted && activity != null &&
            !activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_LOCAL_NETWORK)
        if (granted) onConnect()
    }

    val connectWithPermission = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_LOCAL_NETWORK) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            if (permissionPermanentlyDenied) {
                openAppSettings()
            } else {
                localNetworkPermissionLauncher.launch(Manifest.permission.ACCESS_LOCAL_NETWORK)
            }
        } else {
            permissionDenied = false
            permissionPermanentlyDenied = false
            onConnect()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Connect to WebSocket",
            style = MaterialTheme.typography.titleLarge,
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = host,
            onValueChange = onHostChange,
            label = { Text("IP Address") },
            placeholder = { Text("192.168.1.100") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Next,
            ),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isConnecting,
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = port,
            onValueChange = onPortChange,
            label = { Text("Port") },
            placeholder = { Text("8765") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isConnecting,
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isConnecting) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Connecting...",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            Button(
                onClick = connectWithPermission,
                enabled = host.isNotBlank() && port.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Connect")
            }
        }

        val displayError = if (permissionDenied) {
            "Local network permission is required to connect to LAN devices"
        } else {
            error
        }
        if (displayError != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = displayError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        if (permissionDenied && permissionPermanentlyDenied) {
            TextButton(onClick = openAppSettings) {
                Text("Open Settings to grant permission")
            }
        }
    }
}

@Preview(showBackground = true, name = "Idle - Light")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Idle - Dark")
@Composable
fun WebSocketConnectScreenPreview() {
    MainAndroidTheme {
        WebSocketConnectScreen(
            host = "192.168.1.100",
            port = "8080",
            error = null,
            isConnecting = false,
            onHostChange = {},
            onPortChange = {},
            onConnect = {},
        )
    }
}

@Preview(showBackground = true, name = "Connecting - Light")
@Composable
fun WebSocketConnectingScreenPreview() {
    MainAndroidTheme {
        WebSocketConnectScreen(
            host = "192.168.1.100",
            port = "8080",
            error = null,
            isConnecting = true,
            onHostChange = {},
            onPortChange = {},
            onConnect = {},
        )
    }
}

@Preview(showBackground = true, name = "Error - Light")
@Composable
fun WebSocketConnectScreenErrorPreview() {
    MainAndroidTheme {
        WebSocketConnectScreen(
            host = "192.168.1.100",
            port = "8080",
            error = "Connection refused",
            isConnecting = false,
            onHostChange = {},
            onPortChange = {},
            onConnect = {},
        )
    }
}
