package com.morshues.morshuesandroid.data.model

import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody

data class FileEntry(
    var name: String,
    var size: Long = 0,
    @SerializedName("mtimeMs")
    var modifiedAt: Number = 0,
)

data class ListFolderResponse(
    var ok: Boolean,
    var entries: List<FileEntry>,
)

data class CompareFolderRequest(
    val entries: List<FileEntry>
)

data class CompareFolderResponse(
    var ok: Boolean,
    var upload: List<FileEntry>,
    var download: List<FileEntry>,
)

data class UploadResponse(
    val ok: Boolean,
)

data class RemoteFileResult(
    val responseBody: ResponseBody,
    val modifiedAt: Long?,
    val mimetype: String?,
)