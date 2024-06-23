package com.dhp.musicplayer.core.services.download

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.media3.common.PlaybackException
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.DatabaseProvider
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import com.dhp.musicplayer.core.domain.repository.NetworkMusicRepository
import com.dhp.musicplayer.core.services.di.DownloadCache
import com.dhp.musicplayer.core.services.di.PlayerCache
import com.dhp.musicplayer.core.services.player.PlayableFormatNotFoundException
import com.dhp.musicplayer.core.services.player.RingBuffer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Singleton

@UnstableApi
@Singleton
class DownloadUtil @Inject constructor(
    @ApplicationContext context: Context,
    private val databaseProvider: DatabaseProvider,
    private val networkMusicRepository: NetworkMusicRepository,
    @DownloadCache val downloadCache: SimpleCache,
    @PlayerCache val playerCache: SimpleCache,
) {
    private val ringBuffer = RingBuffer<Pair<String, Uri>?>(2) { null }

    private val dataSourceFactory = ResolvingDataSource.Factory(
        CacheDataSource.Factory()
            .setCache(playerCache)
            .setUpstreamDataSourceFactory(
                OkHttpDataSource.Factory(
                    OkHttpClient.Builder()
                        .build()
                )
            )
    ) { dataSpec ->
        val mediaId = dataSpec.key ?: error("No media id")
        val length = if (dataSpec.length >= 0) dataSpec.length else 1

        if (playerCache.isCached(mediaId, dataSpec.position, length)) {
            return@Factory dataSpec
        }

        when (mediaId) {
            ringBuffer.getOrNull(0)?.first -> return@Factory dataSpec.withUri(ringBuffer.getOrNull(0)!!.second)
            ringBuffer.getOrNull(1)?.first -> return@Factory dataSpec.withUri(ringBuffer.getOrNull(1)!!.second)
        }

        val playerResponse = runBlocking(Dispatchers.IO) {
            networkMusicRepository.player(id = mediaId, idDownload = true)
        }

        val urlResult = runCatching {
            if (playerResponse?.status == "OK") {
                playerResponse.url ?: throw PlayableFormatNotFoundException()

            } else {
                throw PlaybackException(
                    playerResponse?.status,
                    null,
                    PlaybackException.ERROR_CODE_REMOTE_ERROR
                )
            }
        }

        urlResult.getOrThrow().let { url ->
            ringBuffer.append(mediaId to url.toUri())
            dataSpec.withUri(url.toUri())
        }
    }
    val downloadNotificationHelper =
        DownloadNotificationHelper(context, ExoDownloadService.CHANNEL_DOWNLOAD_ID)
    val downloadManager: DownloadManager = DownloadManager(
        context,
        databaseProvider,
        downloadCache,
        dataSourceFactory,
        Executor(Runnable::run)
    ).apply {
        maxParallelDownloads = 3
        addListener(
            ExoDownloadService.TerminalStateNotificationHelper(
                context = context,
                notificationHelper = downloadNotificationHelper,
            )
        )
    }
    val downloads = MutableStateFlow<Map<String, Download>>(emptyMap())

    fun getDownload(songId: String): Flow<Download?> = downloads.map { it[songId] }

    init {
        val result = mutableMapOf<String, Download>()
        val cursor = downloadManager.downloadIndex.getDownloads()
        while (cursor.moveToNext()) {
            result[cursor.download.request.id] = cursor.download
        }
        downloads.value = result
        downloadManager.addListener(
            object : DownloadManager.Listener {
                override fun onDownloadChanged(
                    downloadManager: DownloadManager,
                    download: Download,
                    finalException: Exception?
                ) {
                    downloads.update { map ->
                        map.toMutableMap().apply {
                            set(download.request.id, download)
                        }
                    }
                }
            }
        )
    }
}