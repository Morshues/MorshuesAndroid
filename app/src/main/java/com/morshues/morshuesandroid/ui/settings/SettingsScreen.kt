package com.morshues.morshuesandroid.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.morshues.morshuesandroid.R
import com.morshues.morshuesandroid.settings.SettingsManager
import com.morshues.morshuesandroid.ui.components.CommonConfirmDialog
import com.morshues.morshuesandroid.ui.components.CommonTopBar
import com.morshues.morshuesandroid.ui.components.CommonEditStringDialog
import com.morshues.morshuesandroid.ui.theme.MainAndroidTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    onErrorDismissed: () -> Unit = {},
    uiState: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddUrlDialog by rememberSaveable { mutableStateOf(false) }
    var urlToDelete by rememberSaveable { mutableStateOf<String?>(null) }

    var rootUrlExpanded by rememberSaveable { mutableStateOf(true) }
    var syncNetworkExpanded by rememberSaveable { mutableStateOf(true) }
    var syncModeExpanded by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "Dismiss"
            )
            onErrorDismissed()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            CommonTopBar(
                navController = navController,
                title = "Settings",
                showNavigation = true,
                onNavigationClick = { navController.popBackStack() },
                showSettingsButton = false,
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Root URL Section
            CollapsibleSection(
                title = "Root URL",
                expanded = rootUrlExpanded,
                onToggle = { rootUrlExpanded = !rootUrlExpanded },
                trailingIcon = {
                    IconButton(onClick = { showAddUrlDialog = true }) {
                        Icon(
                            painter = painterResource(R.drawable.round_add_24),
                            contentDescription = "Add URL",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            ) {
                if (uiState.rootUrlList.isEmpty()) {
                    Text(
                        text = "No URLs configured. Tap + to add one.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.rootUrlList.forEach { url ->
                            RootUrlItem(
                                url = url,
                                isSelected = url == uiState.currentServerPath,
                                onSelect = { onAction(SettingsAction.ServerUrl.Select(url)) },
                                onDelete = { urlToDelete = url }
                            )
                        }
                    }
                }
            }

            // Network Type Section
            CollapsibleSection(
                title = "Sync Network",
                expanded = syncNetworkExpanded,
                onToggle = { syncNetworkExpanded = !syncNetworkExpanded },
                description = "Control when files can be uploaded and downloaded",
                modifier = Modifier.padding(top = 24.dp)
            ) {
                NetworkTypeItem(
                    label = "WiFi Only",
                    description = "Sync only when connected to WiFi",
                    isSelected = uiState.syncNetworkType == SettingsManager.NETWORK_TYPE_WIFI_ONLY,
                    onSelect = {
                        onAction(SettingsAction.SyncNetwork.SetNetworkType(SettingsManager.NETWORK_TYPE_WIFI_ONLY))
                    }
                )

                NetworkTypeItem(
                    label = "WiFi or Mobile Data",
                    description = "Sync on any network connection (may use mobile data)",
                    isSelected = uiState.syncNetworkType == SettingsManager.NETWORK_TYPE_ANY,
                    onSelect = {
                        onAction(SettingsAction.SyncNetwork.SetNetworkType(SettingsManager.NETWORK_TYPE_ANY))
                    }
                )
            }

            // Sync Mode Section
            CollapsibleSection(
                title = "Sync Mode",
                expanded = syncModeExpanded,
                onToggle = { syncModeExpanded = !syncModeExpanded },
                description = "Choose which sync operations are allowed",
                modifier = Modifier.padding(top = 24.dp)
            ) {
                SyncModeItem(
                    label = "Full Sync",
                    description = "Upload and download files",
                    isSelected = uiState.syncMode == SettingsManager.SYNC_MODE_FULL,
                    onSelect = {
                        onAction(SettingsAction.SyncMode.SetMode(SettingsManager.SYNC_MODE_FULL))
                    }
                )

                SyncModeItem(
                    label = "Download Only",
                    description = "Only download files from server",
                    isSelected = uiState.syncMode == SettingsManager.SYNC_MODE_DOWNLOAD_ONLY,
                    onSelect = {
                        onAction(SettingsAction.SyncMode.SetMode(SettingsManager.SYNC_MODE_DOWNLOAD_ONLY))
                    }
                )

                SyncModeItem(
                    label = "Upload Only",
                    description = "Only upload files to server",
                    isSelected = uiState.syncMode == SettingsManager.SYNC_MODE_UPLOAD_ONLY,
                    onSelect = {
                        onAction(SettingsAction.SyncMode.SetMode(SettingsManager.SYNC_MODE_UPLOAD_ONLY))
                    }
                )

                SyncModeItem(
                    label = "Disabled",
                    description = "Stop all syncing operations",
                    isSelected = uiState.syncMode == SettingsManager.SYNC_MODE_DISABLED,
                    onSelect = {
                        onAction(SettingsAction.SyncMode.SetMode(SettingsManager.SYNC_MODE_DISABLED))
                    }
                )
            }
        }
    }

    if (showAddUrlDialog) {
        CommonEditStringDialog(
            title = "Add URL",
            initialValue = "",
            placeholder = "https://example.com",
            onConfirm = { newValue ->
                onAction(SettingsAction.ServerUrl.Add(newValue))
                showAddUrlDialog = false
            },
            onDismiss = { showAddUrlDialog = false }
        )
    }

    urlToDelete?.let { url ->
        CommonConfirmDialog(
            title = "Delete URL",
            content = "Are you sure you want to delete \"$url\"?",
            onConfirm = {
                onAction(SettingsAction.ServerUrl.Delete(url))
                urlToDelete = null
            },
            onDismiss = { urlToDelete = null }
        )
    }
}

@Composable
fun CollapsibleSection(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
                description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                trailingIcon?.invoke()

                val rotation by animateFloatAsState(
                    targetValue = if (expanded) 180f else 0f,
                    label = "expand_icon_rotation"
                )
                Icon(
                    painter = painterResource(R.drawable.round_keyboard_arrow_down_24),
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotation)
                )
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier.padding(top = 8.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun RootUrlItem(
    url: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelect() }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = url,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp).weight(1f)
            )
            IconButton(
                modifier = Modifier.size(24.dp),
                onClick = onDelete
            ) {
                Icon(
                    painter = painterResource(R.drawable.rounded_delete_24),
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun NetworkTypeItem(
    label: String,
    description: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelect() }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                modifier = Modifier.size(24.dp)
            )
            Column(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .weight(1f)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SyncModeItem(
    label: String,
    description: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelect() }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                modifier = Modifier.size(24.dp)
            )
            Column(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .weight(1f)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Urls List - Light")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Urls List - Dark")
@Composable
fun SettingsScreenPreview() {
    MainAndroidTheme {
        SettingsScreen(
            navController = rememberNavController(),
            uiState = SettingsUiState(
                currentServerPath = "http://192.168.1.126:3000/",
                rootUrlList = listOf(
                    "http://192.168.1.126:3000/",
                    "https://api.example.com/",
                    "http://localhost:3000/",
                    "https://www.thisIsALongUrlThatWillBreakTheLine.com/",
                )
            ),
            onAction = {}
        )
    }
}

@Preview(showBackground = true, name = "Empty State - Light")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Empty State - Dark")
@Composable
fun SettingsScreenEmptyPreview() {
    MainAndroidTheme {
        SettingsScreen(
            navController = rememberNavController(),
            uiState = SettingsUiState(
                rootUrlList = emptyList()
            ),
            onAction = {}
        )
    }
}
