package com.dhp.musicplayer.player

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dhp.musicplayer.Constants
import com.dhp.musicplayer.MainActivity
import com.dhp.musicplayer.R
import com.dhp.musicplayer.utils.Log
import com.dhp.musicplayer.utils.Theming
import com.dhp.musicplayer.utils.Versioning

class MusicNotificationManager(private val playerService: PlayerService) {

    private val mNotificationManagerCompat get() = NotificationManagerCompat.from(playerService)
    private lateinit var mNotificationBuilder: NotificationCompat.Builder
    private val mNotificationActions
        @SuppressLint("RestrictedApi")
        get() = mNotificationBuilder.mActions
    private val mMediaPlayerHolder get() = MediaPlayerHolder.getInstance()

    fun createNotification(onCreated: (Notification) -> Unit) {
        Log.d("createNotification")

        mNotificationBuilder =
            NotificationCompat.Builder(playerService, Constants.NOTIFICATION_CHANNEL_ID)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(Constants.NOTIFICATION_CHANNEL_ID)
        }

        val openPlayerIntent = Intent(playerService, MainActivity::class.java)
        openPlayerIntent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        var flags = 0
        if (Versioning.isMarshmallow()) flags = PendingIntent.FLAG_IMMUTABLE or 0
        val contentIntent = PendingIntent.getActivity(
            playerService, Constants.NOTIFICATION_INTENT_REQUEST_CODE,
            openPlayerIntent, flags
        )

        mNotificationBuilder
            .setContentIntent(contentIntent)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSilent(true)
            .setShowWhen(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setLargeIcon(null)
            .setOngoing(mMediaPlayerHolder.isPlaying)
            .setSmallIcon(R.drawable.ic_music_note)
            .addAction(getNotificationAction(Constants.PREV_ACTION))
            .addAction(getNotificationAction(Constants.PREV_ACTION))
            .addAction(getNotificationAction(Constants.PLAY_PAUSE_ACTION))
            .addAction(getNotificationAction(Constants.NEXT_ACTION))
            .addAction(getNotificationAction(Constants.CLOSE_ACTION))
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(playerService.getMediaSession()?.sessionToken)
                .setShowActionsInCompactView(1, 2, 3)
            )

        updateNotificationContent {
            onCreated(mNotificationBuilder.build())
        }
    }

    private fun getNotificationAction(action: String): NotificationCompat.Action {
        val icon = Theming.getNotificationActionIcon(action, isNotification = true)
        return NotificationCompat.Action.Builder(icon, action, getPendingIntent(action)).build()
    }

    fun updatePlayPauseAction() {
        Log.d("updatePlayPauseAction")
        if (::mNotificationBuilder.isInitialized) {
            mNotificationActions[2] =
                getNotificationAction(Constants.PLAY_PAUSE_ACTION)
        }
    }

    fun updateNotificationContent(onDone: (() -> Unit)? = null) {
        mMediaPlayerHolder.getMediaMetadataCompat()?.run {
            mNotificationBuilder
                .setContentText(getText(MediaMetadataCompat.METADATA_KEY_ARTIST))
                .setContentTitle(getText(MediaMetadataCompat.METADATA_KEY_TITLE))
                .setSubText(getText(MediaMetadataCompat.METADATA_KEY_ALBUM))
                .setLargeIcon(getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART))
        }
        onDone?.invoke()
    }

    fun updateNotification() {
        if (::mNotificationBuilder.isInitialized) {
            //mNotificationBuilder.setOngoing(mMediaPlayerHolder.isPlaying)
            updatePlayPauseAction()
            with(mNotificationManagerCompat) {
                if (ActivityCompat.checkSelfPermission(
                        playerService,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                notify(Constants.NOTIFICATION_ID, mNotificationBuilder.build())
            }
        }
    }

    private fun getPendingIntent(playerAction: String): PendingIntent {
        val intent = Intent().apply {
            action = playerAction
            component = ComponentName(playerService, PlayerService::class.java)
        }
        var flags = PendingIntent.FLAG_UPDATE_CURRENT
        if (Versioning.isMarshmallow()) {
            flags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getService(playerService, Constants.NOTIFICATION_INTENT_REQUEST_CODE, intent, flags)
    }

    @TargetApi(Build.VERSION_CODES.S)
    fun createNotificationForError() {

        val notificationBuilder =
            NotificationCompat.Builder(playerService, Constants.NOTIFICATION_CHANNEL_ERROR_ID)

        createNotificationChannel(Constants.NOTIFICATION_CHANNEL_ERROR_ID)

        notificationBuilder.setSmallIcon(R.drawable.ic_report)
            .setSilent(true)
            .setContentTitle(playerService.getString(R.string.error_fs_not_allowed_sum))
            .setContentText(playerService.getString(R.string.error_fs_not_allowed))
            .setStyle(
                NotificationCompat.BigTextStyle()
                .bigText(playerService.getString(R.string.error_fs_not_allowed)))
            .priority = NotificationCompat.PRIORITY_DEFAULT
        with(NotificationManagerCompat.from(playerService)) {
            if (ActivityCompat.checkSelfPermission(
                    playerService,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(1, notificationBuilder.build())
        }
    }

    fun cancelNotification() {
        with(mNotificationManagerCompat) {
            cancel(Constants.NOTIFICATION_ID)
        }
    }

    @TargetApi(26)
    private fun createNotificationChannel(id: String) {
        val name = playerService.getString(R.string.app_name)
        val channel = NotificationChannelCompat.Builder(id, NotificationManager.IMPORTANCE_LOW)
            .setName(name)
            .setLightsEnabled(false)
            .setVibrationEnabled(false)
            .setShowBadge(false)
            .build()

        // Register the channel with the system
        mNotificationManagerCompat.createNotificationChannel(channel)
    }
}