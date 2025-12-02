package com.morshues.morshuesandroid.data.repository

import android.content.Context
import android.media.MediaScannerConnection
import com.morshues.morshuesandroid.data.model.FileItem
import com.morshues.morshuesandroid.data.model.FolderItem
import com.morshues.morshuesandroid.data.model.StorageItem
import com.morshues.morshuesandroid.data.model.RemoteFileResult
import com.morshues.morshuesandroid.data.model.toStorageItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class LocalFileRepository(
    private val context: Context
) {
    fun listFiles(path: String): List<StorageItem> {
        val dir = File(path)
        return dir.listFiles()
            // Ignore hide files
            ?.filter {
                !it.name.startsWith(".")
            }?.map {
                it.toStorageItem()
            }?.sortedWith(
                compareBy(
                    { item ->
                        when (item) {
                            is FolderItem -> 0
                            is FileItem -> 1
                        }
                    },
                    { it.name },
                    { item ->
                        when (item) {
                            is FileItem -> item.lastModified
                            is FolderItem -> Long.MAX_VALUE
                        }
                    }
                )
            ) ?: emptyList()
    }

    suspend fun writeFile(
        targetPath: String,
        remoteFileResult: RemoteFileResult
    ) = withContext(Dispatchers.IO) {
        remoteFileResult.responseBody.use { streamingBody ->
            streamingBody.byteStream().use { input ->
                FileOutputStream(targetPath).use { output -> input.copyTo(output) }
            }
        }
        remoteFileResult.modifiedAt?.let {
            File(targetPath).setLastModified(it)
        }

        // scan the downloaded file for showing in photo album
        MediaScannerConnection.scanFile(
            context,
            arrayOf(targetPath),
            arrayOf(remoteFileResult.mimetype),
            null
        )
    }
}