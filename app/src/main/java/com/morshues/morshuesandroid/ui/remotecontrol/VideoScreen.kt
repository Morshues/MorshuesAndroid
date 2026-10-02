package com.morshues.morshuesandroid.ui.remotecontrol

import android.os.SystemClock
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.morshues.morshuesandroid.R
import com.morshues.morshuesandroid.data.websocket.WebSocketManager.VideoState
import com.morshues.morshuesandroid.ui.theme.MainAndroidTheme
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private val SEEK_SECONDS_OPTIONS = listOf(5, 10, 15, 20, 30, 60, 120, 300)
private val SPEED_OPTIONS = listOf(0.5f, 1f, 1.25f, 1.5f, 2f)

data class VideoActions(
    val onPlayPause: () -> Unit,
    val onSeek: (Long) -> Unit,
    val onSeekTo: (Long) -> Unit,
    val onPrevious: () -> Unit,
    val onNext: () -> Unit,
    val onSpeed: (Float) -> Unit,
    val onRequestState: () -> Unit,
    val onSeekSecondsChange: (Int) -> Unit,
)

@Composable
fun VideoScreen(
    videoState: VideoState?,
    seekSeconds: Int,
    actions: VideoActions,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) { actions.onRequestState() }

    if (videoState == null) {
        Text(
            text = "Waiting for video state…",
            modifier = modifier,
            style = MaterialTheme.typography.bodyMedium,
        )
        return
    }

    // The server only broadcasts on events, so advance the position locally while playing
    var now by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    LaunchedEffect(videoState) {
        now = SystemClock.elapsedRealtime()
        while (videoState.isPlaying) {
            delay(500)
            now = SystemClock.elapsedRealtime()
        }
    }
    val duration = videoState.durationMs
    val position = if (videoState.isPlaying && videoState.receivedAt > 0) {
        videoState.positionMs + ((now - videoState.receivedAt) * videoState.speed).toLong()
    } else {
        videoState.positionMs
    }.coerceIn(0, duration.coerceAtLeast(0))

    var dragPosition by remember { mutableStateOf<Float?>(null) }
    val seekStepMs = seekSeconds * 1000L

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = videoState.title.ifBlank { "Untitled" },
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (videoState.count > 0) {
            Text(
                text = "${videoState.index + 1} / ${videoState.count}",
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Slider(
                value = dragPosition ?: position.toFloat(),
                onValueChange = { dragPosition = it },
                onValueChangeFinished = {
                    dragPosition?.let { actions.onSeekTo(it.toLong()) }
                    dragPosition = null
                },
                valueRange = 0f..duration.coerceAtLeast(1).toFloat(),
                enabled = duration > 0,
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = formatTime(dragPosition?.toLong() ?: position),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = formatTime(duration),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconButton(
                onClick = actions.onPrevious,
                enabled = videoState.index > 0,
            ) {
                Icon(painterResource(R.drawable.round_skip_previous_24), contentDescription = "Previous")
            }
            IconButton(
                onClick = { actions.onSeek(-seekStepMs) },
            ) {
                Icon(painterResource(R.drawable.round_fast_rewind_24), contentDescription = "Rewind ${formatSeekStep(seekSeconds)}")
            }
            FilledIconButton(
                onClick = actions.onPlayPause,
                modifier = Modifier.size(64.dp),
            ) {
                Icon(
                    painter = painterResource(
                        if (videoState.isPlaying) R.drawable.round_pause_24 else R.drawable.round_play_arrow_24
                    ),
                    contentDescription = if (videoState.isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(36.dp),
                )
            }
            IconButton(
                onClick = { actions.onSeek(seekStepMs) },
            ) {
                Icon(painterResource(R.drawable.round_fast_forward_24), contentDescription = "Forward ${formatSeekStep(seekSeconds)}")
            }
            IconButton(
                onClick = actions.onNext,
                enabled = videoState.index < videoState.count - 1,
            ) {
                Icon(painterResource(R.drawable.round_skip_next_24), contentDescription = "Next")
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Seek step: ${formatSeekStep(seekSeconds)}",
                style = MaterialTheme.typography.bodySmall,
            )
            val seekIndex = SEEK_SECONDS_OPTIONS.indexOf(seekSeconds)
                .takeIf { it >= 0 }
                ?: SEEK_SECONDS_OPTIONS.indexOfFirst { it >= seekSeconds }.takeIf { it >= 0 }
                ?: SEEK_SECONDS_OPTIONS.lastIndex
            Slider(
                value = seekIndex.toFloat(),
                onValueChange = { actions.onSeekSecondsChange(SEEK_SECONDS_OPTIONS[it.roundToInt()]) },
                valueRange = 0f..SEEK_SECONDS_OPTIONS.lastIndex.toFloat(),
                steps = SEEK_SECONDS_OPTIONS.size - 2,
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                SEEK_SECONDS_OPTIONS.forEachIndexed { index, seconds ->
                    Text(
                        text = formatSeekStep(seconds),
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = when (index) {
                            0 -> TextAlign.Start
                            SEEK_SECONDS_OPTIONS.lastIndex -> TextAlign.End
                            else -> TextAlign.Center
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SPEED_OPTIONS.forEach { speed ->
                FilterChip(
                    selected = videoState.speed == speed,
                    onClick = { actions.onSpeed(speed) },
                    label = { Text("${speed}x".replace(".0x", "x")) },
                )
            }
        }
    }
}

private fun formatSeekStep(seconds: Int): String =
    if (seconds >= 60 && seconds % 60 == 0) "${seconds / 60}min" else "${seconds}s"

private fun formatTime(ms: Long): String {
    val totalSeconds = ms.coerceAtLeast(0) / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}

@Preview(showBackground = true, name = "Light")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark")
@Composable
fun VideoScreenPreview() {
    MainAndroidTheme {
        VideoScreen(
            videoState = VideoState(
                title = "Sample Video",
                index = 1,
                count = 5,
                positionMs = 83_000,
                durationMs = 1_520_000,
                isPlaying = false,
                speed = 1f,
            ),
            seekSeconds = 10,
            actions = VideoActions({}, {}, {}, {}, {}, {}, {}, {}),
        )
    }
}
