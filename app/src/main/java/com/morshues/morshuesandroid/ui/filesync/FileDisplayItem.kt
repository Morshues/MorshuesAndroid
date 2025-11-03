package com.morshues.morshuesandroid.ui.filesync

data class FileDisplayItem(
    val name: String,
    val isDir: Boolean,
    val sizeBytes: Long,
    val lastModified: Long,
)