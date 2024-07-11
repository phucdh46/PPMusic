package com.dhp.musicplayer.utils

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import com.dhp.musicplayer.MainActivity
import com.dhp.musicplayer.core.common.constants.ExtraParameterEnum
import com.dhp.musicplayer.core.designsystem.R
import com.dhp.musicplayer.core.model.music.Song

@UnstableApi
class NotificationHelper(
    private val context: Context,
    private val channelId: String = CHANNEL_ID,
    private val channelName: String = CHANNEL_NAME
) {
    private val notificationIntent: Intent = Intent(context, MainActivity::class.java)

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        )
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    @SuppressLint("MissingPermission")
    fun showSongNotification(title: String?, body: String?, song: Song) {
        notificationIntent.apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(ExtraParameterEnum.NotificationContentKey.pName, song)
        }

        val notification = createDefaultNotification(title, body, notificationIntent)
        if (PermissionsManager.isPostNotificationPermissionGranted(context))
            NotificationManagerCompat.from(context)
                .notify(System.currentTimeMillis().toInt(), notification)
    }


    private fun createDefaultNotification(
        title: String?,
        body: String?,
        intent: Intent,
    ): Notification {
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(
                ContextCompat.getColor(
                    context,
                    com.dhp.musicplayer.R.color.ic_background_launcher
                )
            )
            .setContentIntent(pendingIntent)
            .setDefaults(Notification.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentTitle(title)
            .setContentText(body)
            .build()
    }

    companion object {
        const val CHANNEL_ID = "Music Notification Channel"
        const val CHANNEL_NAME = "Music Notification"
    }
}
