package com.morshues.morshuesandroid.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "syncing_folders")
data class SyncingFolder(
    @PrimaryKey val path: String,
    val createdAt: Long = System.currentTimeMillis(),
    val lastScanned: Long = 0,
)