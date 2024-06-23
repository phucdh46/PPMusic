package com.dhp.musicplayer.core.services.download

import android.app.Notification
import android.content.Context
import androidx.media3.common.util.NotificationUtil
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.PlatformScheduler
import androidx.media3.exoplayer.scheduler.Scheduler
import com.dhp.musicplayer.core.services.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class ExoDownloadService : DownloadService(
    NOTIFICATION_DOWNLOAD_ID,
    1000L,
    CHANNEL_DOWNLOAD_ID,
    R.string.download,
    0
) {
    @Inject
    lateinit var downloadUtil: DownloadUtil

    override fun getDownloadManager() = downloadUtil.downloadManager

    override fun getScheduler(): Scheduler = PlatformScheduler(this, JOB_ID)

    override fun getForegroundNotification(
        downloads: MutableList<Download>,
        notMetRequirements: Int
    ): Notification =
        downloadUtil.downloadNotificationHelper.buildProgressNotification(
            this,
            R.drawable.download,
            null,
            if (downloads.size == 1) Util.fromUtf8Bytes(downloads[0].request.data)
            else resources.getQuantityString(R.plurals.n_song, downloads.size, downloads.size),
            downloads,
            notMetRequirements
        )

    /**
     * This helper will outlive the lifespan of a single instance of [ExoDownloadService]
     */
    class TerminalStateNotificationHelper(
        private val context: Context,
        private val notificationHelper: DownloadNotificationHelper,
    ) : DownloadManager.Listener {
        override fun onDownloadChanged(
            downloadManager: DownloadManager,
            download: Download,
            finalException: Exception?,
        ) {
            if (download.state == Download.STATE_FAILED) {
                val notification = notificationHelper.buildDownloadFailedNotification(
                    context,
                    R.drawable.error,
                    null,
                    Util.fromUtf8Bytes(download.request.data)
                )
                NotificationUtil.setNotification(
                    context,
                    NOTIFICATION_DOWNLOAD_ERROR_ID,
                    notification
                )
            }
        }
    }

    companion object {
        const val CHANNEL_DOWNLOAD_ID = "download"
        const val NOTIFICATION_DOWNLOAD_ID = 54321
        const val NOTIFICATION_DOWNLOAD_ERROR_ID = 54320
        const val JOB_ID = 1
    }
}