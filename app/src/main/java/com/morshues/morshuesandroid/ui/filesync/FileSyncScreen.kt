package com.morshues.morshuesandroid.ui.filesync

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import com.morshues.morshuesandroid.R
import com.morshues.morshuesandroid.data.db.entity.SyncingFolder
import com.morshues.morshuesandroid.data.model.FileItem
import com.morshues.morshuesandroid.data.model.FolderItem
import com.morshues.morshuesandroid.data.model.StorageItem
import com.morshues.morshuesandroid.ui.theme.MainAndroidTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileSyncScreen(
    navController: NavController,
    uiState: FileSyncUiState,
    onFileItemSelected: (StorageItem) -> Unit,
    onBackward: () -> Boolean,
    setSyncingFolder: (String, Boolean) -> Unit,
    onErrorDismissed: () -> Unit = {},
) {
    val syncingFolderPaths = uiState.syncingFolderPaths
    val isCurrentFolderSyncing = uiState.isCurrentFolderSyncing
    val currentFolderRemoteFiles = uiState.currentFolderRemoteFilesSet
    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler {
        if (onBackward().not()) {
            navController.popBackStack()
        }
    }

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
            TopAppBar(
                title= {
                    Text(
                        text = uiState.breadCrumbs.joinToString(" / ") { it.name },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                actions = {
                    if (uiState.isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(
                         onClick = {

                         },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_menu_24),
                            contentDescription = "Sync",
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        val gridState = rememberLazyGridState()
        LazyVerticalGrid(
            modifier = Modifier.padding(paddingValues),
            columns = GridCells.Adaptive(minSize = 150.dp),
            state = gridState,
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items=uiState.files) { file ->
                val isSyncing = if (file is FileItem) {
                    file.name in currentFolderRemoteFiles
                } else {
                    file.path in syncingFolderPaths
                }

                Card(
                    onClick = { onFileItemSelected(file) },
                ) {
                    Column(Modifier.padding(10.dp)) {
                        when (file) {
                            is FolderItem -> {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_folder_24),
                                    contentDescription = file.name,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                )
                            }
                            is FileItem -> {
                                AsyncImage(
                                    model = file.path,
                                    contentDescription = file.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f),
                                    contentScale = ContentScale.Crop,
                                    placeholder = painterResource(R.drawable.outline_lab_profile_24),
                                    error = painterResource(R.drawable.outline_lab_profile_24),
                                    fallback = painterResource(R.drawable.outline_lab_profile_24),
                                )
                            }
                        }
                        Row {
                            Text(
                                text = file.name,
                                fontSize = 18.sp,
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (file is FileItem && isCurrentFolderSyncing) {
                                val syncStatusRes = if (file.name in currentFolderRemoteFiles) {
                                    R.drawable.outline_check_24
                                } else {
                                    R.drawable.outline_cloud_upload_24
                                }
                                Icon(
                                    painter = painterResource(syncStatusRes),
                                    contentDescription = "Sync",
                                    modifier = Modifier.size(24.dp),
                                )
                            } else if (file is FolderItem) {
                                val syncStatusRes = if (isSyncing) {
                                    R.drawable.outline_sync_24
                                } else {
                                    R.drawable.outline_sync_disabled_24
                                }
                                IconButton(
                                    modifier = Modifier.size(24.dp),
                                    onClick = {
                                        setSyncingFolder(file.path, !isSyncing)
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(syncStatusRes),
                                        contentDescription = "Sync",
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun FileSyncScreenPreview() {
    MainAndroidTheme {
        FileSyncScreen(
            navController = rememberNavController(),
            uiState = FileSyncUiState(
                breadCrumbs = ArrayDeque(mockPreviewBreadCrumbs),
                files = mockPreviewItemList(),
                syncingFolders = mockPreviewSyncingFolders(),
                currentFolderRemoteFilesSet = mockPreviewFolderRemoteFiles(),
            ),
            onFileItemSelected = {},
            onBackward = { false },
            setSyncingFolder = { _,_ -> },
            onErrorDismissed = {},
        )
    }
}

@Preview(showBackground = true, name = "Loading State - Light")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Loading State - Dark")
@Composable
fun FileSyncScreenLoadingPreview() {
    MainAndroidTheme {
        FileSyncScreen(
            navController = rememberNavController(),
            uiState = FileSyncUiState(
                breadCrumbs = ArrayDeque(mockPreviewBreadCrumbs),
                files = mockPreviewItemList(),
                syncingFolders = mockPreviewSyncingFolders(),
                currentFolderRemoteFilesSet = mockPreviewFolderRemoteFiles(),
                isProcessing = true,
            ),
            onFileItemSelected = {},
            onBackward = { false },
            setSyncingFolder = { _,_ -> },
            onErrorDismissed = {},
        )
    }
}

@Preview(showBackground = true, name = "Error State - Light")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Error State - Dark")
@Composable
fun FileSyncScreenErrorPreview() {
    MainAndroidTheme {
        FileSyncScreen(
            navController = rememberNavController(),
            uiState = FileSyncUiState(
                breadCrumbs = ArrayDeque(mockPreviewBreadCrumbs),
                files = mockPreviewItemList(),
                syncingFolders = mockPreviewSyncingFolders(),
                currentFolderRemoteFilesSet = mockPreviewFolderRemoteFiles(),
                errorMessage = "Failed to sync folder: Network error",
            ),
            onFileItemSelected = {},
            onBackward = { false },
            setSyncingFolder = { _,_ -> },
            onErrorDismissed = {},
        )
    }
}

private val mockPreviewBreadCrumbs = listOf(
    FolderItem(
        name = "parent folder",
        path = "path-",
    ),
    FolderItem(
        name = "currentFolder",
        path = "path0",
    ),
)

private fun mockPreviewItemList(): List<StorageItem> {
    return listOf(
        FolderItem(
            name = "item1",
            path = "path1",
        ),
        FolderItem(
            name = "A_very_long_folder_name",
            path = "path2",
        ),
        FolderItem(
            name = "item3",
            path = "path3",
        ),
        FileItem(
            name = "item4",
            path = "path4",
            66L,
            99L,
        ),
        FileItem(
            name = "item5",
            path = "path5",
            66L,
            99L,
        ),
        FileItem(
            name = "item6",
            path = "path6",
            66L,
            99L,
        ),
    )
}

private fun mockPreviewSyncingFolders(): List<SyncingFolder> {
    return listOf(
        SyncingFolder(path = "path0"),
        SyncingFolder(path = "path1"),
        SyncingFolder(path = "path3"),
    )
}

private fun mockPreviewFolderRemoteFiles(): Set<String> {
    return setOf(
        "item5",
        "item6"
    )
}
