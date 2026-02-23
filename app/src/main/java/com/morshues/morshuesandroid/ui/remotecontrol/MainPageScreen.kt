package com.morshues.morshuesandroid.ui.remotecontrol

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
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

data class MainPageActions(
    val onUrlChange: (String) -> Unit,
    val onSendUrl: () -> Unit,
    val onNavigate: (DPadDirection) -> Unit,
)

@Composable
fun MainPageScreen(
    url: String,
    actions: MainPageActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = url,
                onValueChange = actions.onUrlChange,
                label = { Text("URL") },
                placeholder = { Text("https://example.com") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done,
                ),
                modifier = Modifier.weight(1f),
            )
            Button(
                onClick = actions.onSendUrl,
                enabled = url.isNotBlank(),
            ) {
                Text("Send")
            }
        }

        DPadScreen(
            onNavigate = actions.onNavigate,
        )
    }
}

@Preview(showBackground = true, name = "With URL - Light")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "With URL - Dark")
@Composable
fun MainPageScreenPreview() {
    MainAndroidTheme {
        MainPageScreen(
            url = "https://example.com",
            actions = MainPageActions({}, {}, {}),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, name = "Empty URL - Light")
@Composable
fun MainPageScreenEmptyPreview() {
    MainAndroidTheme {
        MainPageScreen(
            url = "",
            actions = MainPageActions({}, {}, {}),
            modifier = Modifier.padding(16.dp),
        )
    }
}
