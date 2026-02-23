package com.morshues.morshuesandroid.ui.remotecontrol

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.morshues.morshuesandroid.R
import com.morshues.morshuesandroid.ui.theme.MainAndroidTheme

enum class DPadDirection { UP, DOWN, LEFT, RIGHT, OK }

private val ButtonSize = 64.dp
private val IconSize = 56.dp

@Composable
fun DPadScreen(
    onNavigate: (DPadDirection) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(
            onClick = { onNavigate(DPadDirection.UP) },
            modifier = Modifier.size(ButtonSize),
            contentPadding = PaddingValues(0.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.round_arrow_drop_up_24),
                contentDescription = "Up",
                modifier = Modifier.size(IconSize),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { onNavigate(DPadDirection.LEFT) },
                modifier = Modifier.size(ButtonSize),
                contentPadding = PaddingValues(0.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.round_arrow_left_24),
                    contentDescription = "Left",
                    modifier = Modifier.size(IconSize),
                )
            }

            Button(
                onClick = { onNavigate(DPadDirection.OK) },
                modifier = Modifier.size(ButtonSize),
                contentPadding = PaddingValues(0.dp),
            ) {
                Text("OK")
            }

            Button(
                onClick = { onNavigate(DPadDirection.RIGHT) },
                modifier = Modifier.size(ButtonSize),
                contentPadding = PaddingValues(0.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.round_arrow_right_24),
                    contentDescription = "Right",
                    modifier = Modifier.size(IconSize),
                )
            }
        }

        Button(
            onClick = { onNavigate(DPadDirection.DOWN) },
            modifier = Modifier.size(ButtonSize),
            contentPadding = PaddingValues(0.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.round_arrow_drop_down_24),
                contentDescription = "Down",
                modifier = Modifier.size(IconSize),
            )
        }
    }
}

@Preview(showBackground = true, name = "Light")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark")
@Composable
fun DPadScreenPreview() {
    MainAndroidTheme {
        DPadScreen(onNavigate = {})
    }
}
