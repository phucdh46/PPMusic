package com.dhp.musicplayer.utils


import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dhp.musicplayer.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object Permissions {

    private val PERMISSION_READ_AUDIO get() = if (Versioning.isTiramisu()) {
        // READ_EXTERNAL_STORAGE was superseded by READ_MEDIA_AUDIO in Android 13
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    @JvmStatic
    fun hasToAskForReadStoragePermission(activity: Activity) =
        Versioning.isMarshmallow() && ContextCompat.checkSelfPermission(
            activity,
            PERMISSION_READ_AUDIO
        ) != PackageManager.PERMISSION_GRANTED

    @JvmStatic
    fun manageAskForReadStoragePermission(
        activity: Activity,
        request: ActivityResultLauncher<String>
    ) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, PERMISSION_READ_AUDIO)) {
            MaterialAlertDialogBuilder(activity)
                .setCancelable(false)
                .setTitle(R.string.app_name)
                .setMessage(R.string.perm_rationale)
                .setPositiveButton(R.string.ok) { _, _ ->
                    requestReadStoragePermission(request)
                }
                .setNegativeButton(R.string.no) { _, _ ->
                    //(activity as UIControlInterface).onDenyPermission()
                }
                .show()
        } else {
            requestReadStoragePermission(request)
        }
    }

    private fun requestReadStoragePermission(request: ActivityResultLauncher<String>) =
        request.launch(PERMISSION_READ_AUDIO)
}
