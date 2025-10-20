package com.morshues.morshuesandroid.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.core.content.ContextCompat

object PermissionHelper {
    enum class MediaAccess {
        NONE,
        LIMITED,
        FULL_MEDIA,
        FULL_FILES,
    }

    fun getCurrentMediaAccess(context: Context): MediaAccess {
        // In some case, user may have full access for files
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
            return MediaAccess.FULL_FILES
        }

        fun granted(p: String) = ContextCompat.checkSelfPermission(context, p) ==
                PackageManager.PERMISSION_GRANTED

        return when {
            // Android 14+: may only have partial access
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                val hasFull = granted(Manifest.permission.READ_MEDIA_IMAGES)
                        && granted(Manifest.permission.READ_MEDIA_VIDEO)
                val hasLimited = granted(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                when {
                    hasFull -> MediaAccess.FULL_MEDIA
                    hasLimited -> MediaAccess.LIMITED
                    else -> MediaAccess.NONE
                }
            }

            // Android 13
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                val hasFull = granted(Manifest.permission.READ_MEDIA_IMAGES)
                        && granted(Manifest.permission.READ_MEDIA_VIDEO)
                if (hasFull) MediaAccess.FULL_MEDIA else MediaAccess.NONE
            }

            // Android 12-
            else -> {
                val hasFull = granted(Manifest.permission.READ_EXTERNAL_STORAGE)
                if (hasFull) MediaAccess.FULL_MEDIA else MediaAccess.NONE
            }
        }
    }

    fun requestMediaAccess(launcher: ManagedActivityResultLauncher<Array<String>, Map<String, @JvmSuppressWildcards Boolean>>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO
                )
            )
        } else {
            launcher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        }
    }
}