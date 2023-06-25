package com.dhp.musicplayer.utils

import android.content.Context
import com.dhp.musicplayer.R
import com.dhp.musicplayer.player.MediaPlayerHolder
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object Dialogs {
    @JvmStatic
    fun stopPlaybackDialog(context: Context) {
        val mediaPlayerHolder = MediaPlayerHolder.getInstance()
//        if (GoPreferences.getPrefsInstance().isAskForRemoval) {
        MaterialAlertDialogBuilder(context)
            .setCancelable(false)
            .setTitle(R.string.app_name)
            .setMessage(R.string.on_close_activity)
            .setPositiveButton(R.string.yes) { _, _ ->
                mediaPlayerHolder.stopPlaybackService(stopPlayback = true, fromUser = true, fromFocus = false)
            }
            .setNegativeButton(R.string.no) { _, _ ->
                mediaPlayerHolder.stopPlaybackService(stopPlayback = false, fromUser = true, fromFocus = false)
            }
            .setNeutralButton(R.string.cancel, null)
            .show()
        return
//        }
        mediaPlayerHolder.stopPlaybackService(stopPlayback = false, fromUser = true, fromFocus = false)
    }
}