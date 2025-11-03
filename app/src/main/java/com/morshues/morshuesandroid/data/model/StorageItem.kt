package com.morshues.morshuesandroid.data.model

import java.io.File

sealed interface StorageItem {
    val name: String
    val path: String
}

data class FileItem(
    override val name: String,
    override val path: String,
    val sizeBytes: Long,
    val lastModified: Long,
): StorageItem {
    companion object {
        fun fromFile(file: File): FileItem {
            require(file.isFile) { "File must be a regular file, not a directory" }
            return FileItem(
                name = file.name,
                path = file.path,
                sizeBytes = file.length(),
                lastModified = file.lastModified(),
            )
        }
    }
}

data class FolderItem(
    override val name: String,
    override val path: String,
): StorageItem {
    companion object {
        fun fromFile(file: File): FolderItem {
            require(file.isDirectory) { "File must be a directory" }
            return FolderItem(
                name = file.name,
                path = file.path,
            )
        }
    }
}

fun File.toStorageItem(): StorageItem {
    return if (isDirectory) {
        FolderItem.fromFile(this)
    } else {
        FileItem.fromFile(this)
    }
}
