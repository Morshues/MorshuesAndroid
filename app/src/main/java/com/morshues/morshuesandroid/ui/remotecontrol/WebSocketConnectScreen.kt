package com.morshues.morshuesandroid.ui.remotecontrol

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
                onClick = onConnect,
                enabled = host.isNotBlank() && port.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Connect")
            }
        }

        if (error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
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
