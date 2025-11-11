package com.morshues.morshuesandroid.ui.components

import android.content.res.Configuration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.morshues.morshuesandroid.ui.theme.MainAndroidTheme

@Composable
fun CommonConfirmDialog(
    title: String,
    content: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(content) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = content.trim().isNotEmpty()
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun CommonConfirmDialogPreview() {
    MainAndroidTheme {
        CommonConfirmDialog(
            title = "Please confirm",
            content = "Going to remove the database",
            onConfirm = {},
            onDismiss = {}
        )
    }
}