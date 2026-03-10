package com.morshues.morshuesandroid.data.repository

import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.Context
import android.content.IntentSender
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
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

    /**
     * Attempts to delete the file at [path].
     * Returns true if deleted successfully.
     * Returns false if the file is owned by another app and requires user confirmation
     * via [getDeleteIntentSender].
     */
    fun deleteFile(path: String): Boolean {
        val file = File(path)
        if (!file.exists()) return true

        if (file.delete()) {
            MediaScannerConnection.scanFile(context, arrayOf(path), null, null)
            return true
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val deleted = context.contentResolver.delete(
                    MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                    "${MediaStore.MediaColumns.DATA} = ?",
                    arrayOf(file.absolutePath),
                )
                if (deleted > 0) return true
            } catch (_: RecoverableSecurityException) {
                // File is owned by another app; caller must use getDeleteIntentSender()
            }
        }

        return false
    }

    /**
     * Returns an [IntentSender] that launches a system dialog asking the user to confirm
     * deletion of a file owned by another app. Only needed when [deleteFile] returns false.
     */
    fun getDeleteIntentSender(path: String): IntentSender? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val uri = getContentUri(path) ?: return null
            return MediaStore.createDeleteRequest(context.contentResolver, listOf(uri)).intentSender
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // On API 29, get the IntentSender from the RecoverableSecurityException itself
            return try {
                context.contentResolver.delete(
                    MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                    "${MediaStore.MediaColumns.DATA} = ?",
                    arrayOf(path),
                )
                null
            } catch (e: RecoverableSecurityException) {
                e.userAction.actionIntent.intentSender
            }
        }
        return null
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getContentUri(path: String): Uri? {
        context.contentResolver.query(
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
            arrayOf(MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.MEDIA_TYPE),
            "${MediaStore.Files.FileColumns.DATA} = ?",
            arrayOf(path), null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
                val mediaType = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE))
                val baseUri = when (mediaType) {
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    else -> MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                }
                return ContentUris.withAppendedId(baseUri, id)
            }
        }
        return null
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