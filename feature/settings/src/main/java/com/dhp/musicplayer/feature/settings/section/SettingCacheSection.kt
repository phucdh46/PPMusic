package com.dhp.musicplayer.feature.settings.section

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.DownloadService
import coil.Coil
import com.dhp.musicplayer.core.datastore.MaxImageCacheSizeKey
import com.dhp.musicplayer.core.datastore.MaxSongCacheSizeKey
import com.dhp.musicplayer.core.ui.LocalPlayerConnection
import com.dhp.musicplayer.feature.settings.R
import com.dhp.musicplayer.core.ui.common.rememberPreference
import com.dhp.musicplayer.core.common.extensions.calculatorPercentCache
import com.dhp.musicplayer.core.common.extensions.formatFileSize
import com.dhp.musicplayer.core.common.utils.tryOrNull
import com.dhp.musicplayer.core.services.download.ExoDownloadService
import com.dhp.musicplayer.feature.settings.items.SettingCacheItem
import com.dhp.musicplayer.feature.settings.items.SettingTopTitleItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@Composable
internal fun SettingCacheSection(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
//    val imageDiskCache = context.imageLoader.diskCache ?: return
    val playerCache = LocalPlayerConnection.current?.playerCache ?: return
    val downloadCache = LocalPlayerConnection.current?.downloadCache ?: return

//    var imageCacheSize by remember(imageDiskCache) {
//        mutableLongStateOf(imageDiskCache.size)
//    }

    var refresh by remember {
        mutableStateOf(false)
    }
    val playerCacheSize by remember(refresh) {
        mutableLongStateOf(tryOrNull { playerCache.cacheSpace } ?: 0)
    }
    val downloadCacheSize by remember(refresh) {
        mutableLongStateOf(tryOrNull { downloadCache.cacheSpace } ?: 0)
    }

    val (maxImageCacheSize, onMaxImageCacheSizeChange) = rememberPreference(key = MaxImageCacheSizeKey, defaultValue = 512)
    val (maxSongCacheSize, onMaxSongCacheSizeChange) = rememberPreference(key = MaxSongCacheSizeKey, defaultValue = 1024)
    val coroutineScope = rememberCoroutineScope()

    Column(modifier) {
        SettingTopTitleItem(
            modifier = Modifier.fillMaxWidth(),
            text = R.string.setting_cache_title,
        )

        SettingCacheItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(id = R.string.setting_download_cache_title),
            description = stringResource(R.string.size_used, formatFileSize(downloadCacheSize)),
            showButtonClear = downloadCacheSize != 0L,
            onConfirmCleanCache = {
                coroutineScope.launch(Dispatchers.IO) {
                    DownloadService.sendRemoveAllDownloads(
                        context,
                        ExoDownloadService::class.java,
                        false
                    )
                    refresh = !refresh
//                    downloadCache.keys.forEach { key ->
//                        downloadCache.removeResource(key)
//                        downloadCacheSize = downloadCache.cacheSpace
//                    }
                }
            }
        )

        SettingCacheItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(id = R.string.setting_song_cache_title),
            description = if (maxSongCacheSize != -1) {
                stringResource(
                    R.string.size_used,
                    "${formatFileSize(playerCacheSize)} / ${formatFileSize(maxSongCacheSize * 1024 * 1024L)}"
                ) + " (${calculatorPercentCache(playerCacheSize, maxSongCacheSize * 1024 * 1024L)})%"
            } else
                stringResource(R.string.size_used, formatFileSize(playerCacheSize)),
            selectedValue = maxSongCacheSize,
            values = listOf(128, 256, 512, 1024, 2048, 4096, 8192, -1),
            valueText = {
                if (it == -1) stringResource(R.string.unlimited) else formatFileSize(it * 1024 * 1024L)
            },
            onValueSelected = onMaxSongCacheSizeChange,
            showButtonClear = playerCacheSize != 0L,
            onConfirmCleanCache = {
                coroutineScope.launch(Dispatchers.IO) {
                    playerCache.keys.forEach { key ->
                        playerCache.removeResource(key)
                        refresh = !refresh
                    }
                }
            }
        )

        Coil.imageLoader(context).diskCache?.let { diskCache ->
            var diskCacheSize = remember(refresh) {
                diskCache.size
            }

            SettingCacheItem(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(id = R.string.setting_image_cache_title),
                description = stringResource(R.string.size_used, "${formatFileSize(diskCacheSize)} / ${formatFileSize(maxImageCacheSize.toLong()  * 1024 * 1024L)}") +
                        " (${calculatorPercentCache(diskCacheSize, maxImageCacheSize * 1024 * 1024L)}%)",
                selectedValue = maxImageCacheSize,
                values = listOf(128, 256, 512, 1024, 2048, 4096, 8192),
                valueText = { formatFileSize(it * 1024 * 1024L) },
                onValueSelected = onMaxImageCacheSizeChange,
                showButtonClear = diskCacheSize != 0L,
                onConfirmCleanCache = {
                    coroutineScope.launch(Dispatchers.IO) {
                        diskCache.clear()
                        diskCacheSize = diskCache.size
                        refresh = !refresh
                    }
                }
            )
        }
    }
}