package com.morshues.morshuesandroid.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.morshues.morshuesandroid.R
import com.morshues.morshuesandroid.ui.theme.MainAndroidTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonTopBar(
    navController: NavController,
    title: String = "",
    titleContent: @Composable (() -> Unit)? = null,
    showNavigation: Boolean = false,
    onNavigationClick: (() -> Unit)? = null,
    showSettingsButton: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = titleContent ?: { Text(title) },
        navigationIcon = {
            if (showNavigation && onNavigationClick != null) {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        painter = painterResource(R.drawable.round_arrow_back_24),
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = {
            actions()
            if (showSettingsButton) {
                IconButton(onClick = { navController.navigate("settings") }) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_menu_24),
                        contentDescription = "Settings",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    )
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode - Error")
@Composable
fun CommonTopBarPreview() {
    MainAndroidTheme {
        CommonTopBar(
            navController = rememberNavController(),
            title = "Preview Title",
            onNavigationClick = {},
            showSettingsButton = true,
        )
    }
}