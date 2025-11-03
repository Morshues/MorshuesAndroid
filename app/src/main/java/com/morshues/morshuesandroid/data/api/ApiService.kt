package com.morshues.morshuesandroid.data.api

import com.morshues.morshuesandroid.data.model.CompareFolderRequest
import com.morshues.morshuesandroid.data.model.CompareFolderResponse
import com.morshues.morshuesandroid.data.model.ListFolderResponse
import com.morshues.morshuesandroid.data.model.UploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Streaming

/**
 * API service for protected endpoints that require authentication.
 * These endpoints use TokenAuthenticator for automatic token refresh.
 */
interface ApiService {

    @GET("api/file-sync/{folderName}/files")
    suspend fun listFolder(
        @Header("Authorization") authorization: String? = null,
        @Path("folderName", encoded = true) folderName: String,
    ): ListFolderResponse

    @POST("api/file-sync/{folderName}/sync")
    suspend fun compareFolder(
        @Header("Authorization") authorization: String? = null,
        @Path("folderName", encoded = true) folderName: String,
        @Body requestBody: CompareFolderRequest,
    ): CompareFolderResponse

    @Multipart
    @POST("api/file-sync/{folderName}/upload")
    suspend fun uploadFile(
        @Header("Authorization") authorization: String? = null,
        @Path("folderName", encoded = true) folderName: String,
        @Part file: MultipartBody.Part,
        @Part("lastModified") lastModified: RequestBody,
    ): UploadResponse

    @Streaming
    @GET("api/file-sync/{folderName}/files/{fileName}/download")
    suspend fun downloadFile(
        @Header("Authorization") authorization: String? = null,
        @Path("folderName", encoded = true) folderName: String,
        @Path("fileName") fileName: String,
    ): Response<ResponseBody>

}