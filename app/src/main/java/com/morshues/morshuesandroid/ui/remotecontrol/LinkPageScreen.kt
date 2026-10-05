package com.morshues.morshuesandroid.ui.remotecontrol

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.morshues.morshuesandroid.ui.theme.MainAndroidTheme
import kotlin.math.roundToInt

private val SCROLL_SCALE_OPTIONS = listOf(20, 50, 100, 200, 500, 1000)

data class LinkPageActions(
    val onJoystickMove: (x: Float, y: Float) -> Unit,
    val onZoom: (Boolean) -> Unit,
    val onBgInv: () -> Unit,
    val onScrollScaleChange: (Int) -> Unit,
)

@Composable
fun LinkPageScreen(
    scrollScale: Int,
    actions: LinkPageActions,
    modifier: Modifier = Modifier,
) {
    var holdScroll by rememberSaveable { mutableStateOf(false) }

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
            holdOnRelease = holdScroll,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Hold scroll")
            Switch(checked = holdScroll, onCheckedChange = { holdScroll = it })
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Scroll scale: $scrollScale",
                style = MaterialTheme.typography.bodySmall,
            )
            val scaleIndex = SCROLL_SCALE_OPTIONS.indexOf(scrollScale)
                .takeIf { it >= 0 }
                ?: SCROLL_SCALE_OPTIONS.indexOfFirst { it >= scrollScale }.takeIf { it >= 0 }
                ?: SCROLL_SCALE_OPTIONS.lastIndex
            Slider(
                value = scaleIndex.toFloat(),
                onValueChange = { actions.onScrollScaleChange(SCROLL_SCALE_OPTIONS[it.roundToInt()]) },
                valueRange = 0f..SCROLL_SCALE_OPTIONS.lastIndex.toFloat(),
                steps = SCROLL_SCALE_OPTIONS.size - 2,
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                SCROLL_SCALE_OPTIONS.forEachIndexed { index, scale ->
                    Text(
                        text = scale.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = when (index) {
                            0 -> TextAlign.Start
                            SCROLL_SCALE_OPTIONS.lastIndex -> TextAlign.End
                            else -> TextAlign.Center
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Light")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark")
@Composable
fun LinkPageScreenPreview() {
    MainAndroidTheme {
        LinkPageScreen(
            scrollScale = 100,
            actions = LinkPageActions({ _, _ -> }, {}, {}, {}),
        )
    }
}
