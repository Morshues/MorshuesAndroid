package com.morshues.morshuesandroid.ui.remotecontrol

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.morshues.morshuesandroid.ui.theme.MainAndroidTheme

data class LinkPageActions(
    val onJoystickMove: (x: Float, y: Float) -> Unit,
    val onZoom: (Boolean) -> Unit,
    val onBgInv: () -> Unit,
)

@Composable
fun LinkPageScreen(
    actions: LinkPageActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { actions.onZoom(true) }) {
                Text("Zoom +")
            }
            Button(onClick = { actions.onZoom(false) }) {
                Text("Zoom -")
            }
            Button(onClick = actions.onBgInv) {
                Text("BG Inv")
            }
        }

        JoystickScreen(
            onJoystickMove = actions.onJoystickMove,
        )
    }
}

@Preview(showBackground = true, name = "Light")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark")
@Composable
fun LinkPageScreenPreview() {
    MainAndroidTheme {
        LinkPageScreen(
            actions = LinkPageActions({ _, _ -> }, {}, {}),
        )
    }
}
