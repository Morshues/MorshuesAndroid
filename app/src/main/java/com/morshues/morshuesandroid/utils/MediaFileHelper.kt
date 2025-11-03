package com.morshues.morshuesandroid.utils

object MediaFileHelper {
    private val IMAGE_EXTENSIONS = setOf(
        "jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "heif"
    )

    private val VIDEO_EXTENSIONS = setOf(
        "mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "m4v", "3gp"
    )

    private val AUDIO_EXTENSIONS = setOf(
        "mp3", "wav", "flac", "aac", "ogg", "m4a", "wma", "opus"
    )

    /**
     * Check if a file is a media file (image, video, or audio)
     */
    fun isMediaFile(fileName: String): Boolean {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return extension in IMAGE_EXTENSIONS
            || extension in VIDEO_EXTENSIONS
            || extension in AUDIO_EXTENSIONS
    }

    /**
     * Check if a file is an image
     */
    fun isImageFile(fileName: String): Boolean {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return extension in IMAGE_EXTENSIONS
    }

    /**
     * Check if a file is a video
     */
    fun isVideoFile(fileName: String): Boolean {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return extension in VIDEO_EXTENSIONS
    }

    /**
     * Check if a file is audio
     */
    fun isAudioFile(fileName: String): Boolean {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return extension in AUDIO_EXTENSIONS
    }
}
