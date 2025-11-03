package com.morshues.morshuesandroid.data.repository

import android.util.Base64
import com.morshues.morshuesandroid.data.api.ApiService
import com.morshues.morshuesandroid.data.model.CompareFolderRequest
import com.morshues.morshuesandroid.data.model.CompareFolderResponse
import com.morshues.morshuesandroid.data.model.FileEntry
import com.morshues.morshuesandroid.data.model.ListFolderResponse
import com.morshues.morshuesandroid.data.model.RemoteFileResult
import com.morshues.morshuesandroid.data.model.UploadResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class RemoteFileRepository(
    private val api: ApiService,
) {
    private fun String.encodeFolderName(): String {
        return Base64.encodeToString(
            this.encodeToByteArray(),
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )
    }

    suspend fun listFolder(folderPath: String): ListFolderResponse {
        return api.listFolder(
            folderName = folderPath.encodeFolderName(),
        )
    }

    suspend fun compareFolder(folderPath: String, entries: List<FileEntry>): CompareFolderResponse {
        return api.compareFolder(
            folderName = folderPath.encodeFolderName(),
            requestBody = CompareFolderRequest(entries),
        )
    }
    suspend fun uploadFile(folderPath: String, path: String): UploadResponse {
        val file = File(path)
        val requestFile = file.asRequestBody("application/octet-stream".toMediaType())
        val multipartBody = MultipartBody.Part.createFormData("file", file.name, requestFile)
        val lastModifiedBody = file.lastModified().toString()
            .toRequestBody("text/plain".toMediaType())
        return api.uploadFile(
            folderName = folderPath.encodeFolderName(),
            file = multipartBody,
            lastModified = lastModifiedBody,
        )
    }

    suspend fun downloadFile(folderPath: String, fileName: String): RemoteFileResult {
        val response = api.downloadFile(
            folderName = folderPath.encodeFolderName(),
            fileName = fileName,
        )
        if (!response.isSuccessful) throw HttpException(response)

        val lastModifiedHeader = response.headers()["Last-Modified"]
        val mimeType = response.headers()["Content-Type"]

        return RemoteFileResult(
            response.body()!!,
            lastModifiedHeader.toHttpDateMillis(),
            mimeType,
        )
    }

    // Could be replaced with DateTimeFormatter.RFC_1123_DATE_TIME if api >= 26
    private fun String?.toHttpDateMillis(): Long? {
        if (this.isNullOrBlank()) return null
        return try {
            val sdf = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("GMT")
            sdf.parse(this)?.time
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}