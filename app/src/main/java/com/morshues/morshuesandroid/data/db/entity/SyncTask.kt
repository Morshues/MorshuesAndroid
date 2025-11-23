package com.morshues.morshuesandroid.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sync_tasks",
    indices = [
        Index(value = ["status", "priority", "createdAt"]),
        Index(value = ["folderPath"]),
        Index(value = ["filePath"]),
    ]
)
data class SyncTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val folderPath: String,
    val fileName: String,
    val filePath: String,
    val syncType: SyncType,
    val status: SyncStatus,
    val priority: Int = 0,
    val fileSize: Long = 0,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val workRequestId: String? = null,
)

enum class SyncType {
    UPLOAD,
    DOWNLOAD
}

enum class SyncStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    CANCELLED
}
