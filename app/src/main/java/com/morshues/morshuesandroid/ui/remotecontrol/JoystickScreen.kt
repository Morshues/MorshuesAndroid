package com.morshues.morshuesandroid.ui.remotecontrol

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.morshues.morshuesandroid.ui.theme.MainAndroidTheme
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private val BaseSize = 200.dp
private val ThumbSize = 64.dp

@Composable
fun JoystickScreen(
    onJoystickMove: (x: Float, y: Float) -> Unit,
    sendIntervalMs: Long = 50L,
) {
    // Visual offset in raw pixels — drives the thumb position on screen
    var thumbOffset by remember { mutableStateOf(Offset.Zero) }
    // Normalized -1..1 value; updated by drag, read by the periodic ticker
    var normalizedOffset by remember { mutableStateOf(Offset.Zero) }

    val currentOnJoystickMove by rememberUpdatedState(onJoystickMove)

    // Periodic ticker: fires at fixed interval and sends the current value while the
    // joystick is held away from center. Skips silently when back at zero.
    LaunchedEffect(sendIntervalMs) {
        while (true) {
            delay(sendIntervalMs)
            val v = normalizedOffset
            if (v != Offset.Zero) {
                currentOnJoystickMove(v.x, v.y)
            }
        }
    }

    val baseColor = MaterialTheme.colorScheme.surfaceVariant
    val baseStrokeColor = MaterialTheme.colorScheme.outline
    val thumbColor = MaterialTheme.colorScheme.primary

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(BaseSize)
            .pointerInput(Unit) {
                val thumbRadiusPx = ThumbSize.toPx() / 2f
                val maxDistance = size.width / 2f - thumbRadiusPx
                val center = Offset(size.width / 2f, size.height / 2f)

                fun updateThumb(position: Offset) {
                    val raw = position - center
                    val distance = raw.getDistance()
                    val clamped = if (distance <= maxDistance) raw
                                  else raw / distance * maxDistance
                    thumbOffset = clamped
                    normalizedOffset = Offset(
                        (clamped.x / maxDistance).coerceIn(-1f, 1f),
                        (clamped.y / maxDistance).coerceIn(-1f, 1f),
                    )
                }

                fun resetThumb() {
                    thumbOffset = Offset.Zero
                    normalizedOffset = Offset.Zero
                    currentOnJoystickMove(0f, 0f)
                }

                detectDragGestures(
                    onDragStart = { updateThumb(it) },
                    onDrag = { change, _ ->
                        change.consume()
                        updateThumb(change.position)
                    },
                    onDragEnd = { resetThumb() },
                    onDragCancel = { resetThumb() },
                )
            },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(color = baseColor, radius = size.width / 2f)
            drawCircle(
                color = baseStrokeColor,
                radius = size.width / 2f,
                style = Stroke(width = 2.dp.toPx()),
            )
        }
        Box(
            modifier = Modifier
                .size(ThumbSize)
                .offset { IntOffset(thumbOffset.x.roundToInt(), thumbOffset.y.roundToInt()) }
                .background(color = thumbColor, shape = CircleShape),
        )
    }
}

@Preview(showBackground = true, name = "Light")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark")
@Composable
fun JoystickScreenPreview() {
    MainAndroidTheme {
        JoystickScreen(onJoystickMove = { _, _ -> })
    }
}
