package com.morshues.morshuesandroid.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.morshues.morshuesandroid.R
import com.morshues.morshuesandroid.ui.theme.MainAndroidTheme

data class MenuItem(
    val text: String,
    val onClick: () -> Unit,
    @param:DrawableRes val iconRes: Int? = null,
    val contentDescription: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonTopBar(
    navController: NavController,
    title: String = "",
    titleContent: @Composable (() -> Unit)? = null,
    showNavigation: Boolean = false,
    onNavigationClick: (() -> Unit)? = null,
    showSettingsButton: Boolean = true,
    statusAction: @Composable RowScope.() -> Unit = {},
    menuItems: List<MenuItem> = emptyList(),
    initialMenuExpanded: Boolean = false
) {
    var showMenu by remember { mutableStateOf(initialMenuExpanded) }

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
            statusAction()
            if (!showSettingsButton) {
                return@TopAppBar
            }

            if (menuItems.isEmpty()) {
                IconButton(onClick = { navController.navigate("settings") }) {
                    Icon(
                        painter = painterResource(R.drawable.rounded_settings_24),
                        contentDescription = "Settings",
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_menu_24),
                        contentDescription = "Menu",
                        modifier = Modifier.size(24.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Settings") },
                        onClick = {
                            showMenu = false
                            navController.navigate("settings")
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.rounded_settings_24),
                                contentDescription = null
                            )
                        }
                    )

                    menuItems.forEach { menuItem ->
                        DropdownMenuItem(
                            text = { Text(menuItem.text) },
                            onClick = {
                                showMenu = false
                                menuItem.onClick()
                            },
                            leadingIcon = menuItem.iconRes?.let {
                                {
                                    Icon(
                                        painter = painterResource(it),
                                        contentDescription = menuItem.contentDescription
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    )
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
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

@Preview(showBackground = true, name = "Menu Expanded - Light")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Menu Expanded - Dark")
@Composable
fun CommonTopBarMenuExpandedPreview() {
    MainAndroidTheme {
        CommonTopBar(
            navController = rememberNavController(),
            title = "Preview Title",
            onNavigationClick = {},
            showSettingsButton = true,
            initialMenuExpanded = true,
            menuItems = listOf(
                MenuItem(
                    text = "About",
                    onClick = { },
                    iconRes = R.drawable.outline_lab_profile_24,
                    contentDescription = "About"
                ),
                MenuItem(
                    text = "Help",
                    onClick = { },
                    iconRes = R.drawable.outline_sync_24,
                    contentDescription = "Help"
                ),
                MenuItem(
                    text = "No Icon Item",
                    onClick = { }
                )
            )
        )
    }
}