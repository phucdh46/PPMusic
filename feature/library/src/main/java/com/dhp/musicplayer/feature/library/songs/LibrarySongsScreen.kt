package com.dhp.musicplayer.feature.library.songs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dhp.musicplayer.core.designsystem.constant.ListThumbnailSize
import com.dhp.musicplayer.core.designsystem.icon.IconApp
import com.dhp.musicplayer.core.model.settings.LibrarySongsDetailType
import com.dhp.musicplayer.core.ui.items.DefaultListItem
import com.dhp.musicplayer.feature.library.R

@Composable
fun LibrarySongsScreen(
    navigateToLibrarySongsDetail: (String) -> Unit,
) {
    LibrarySongsScreen(
        onFavouriteClick = {
            navigateToLibrarySongsDetail(LibrarySongsDetailType.FAVOURITE.name)
        },
        onDownloadedClick = {
            navigateToLibrarySongsDetail(LibrarySongsDetailType.DOWNLOADED.name)
        },
        onDeviceSongsClick = {
            navigateToLibrarySongsDetail(LibrarySongsDetailType.DEVICE_SONGS.name)

        },
    )
}

@Composable
fun LibrarySongsScreen(
    onFavouriteClick: () -> Unit,
    onDownloadedClick: () -> Unit,
    onDeviceSongsClick: () -> Unit,
) {

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        DefaultListItem(
            title = stringResource(id = R.string.title_library_favourites),
            subtitle = stringResource(id = R.string.subtitle_library_favourites),
            thumbnailContent = {
                Icon(
                    imageVector = IconApp.Favorite,
                    tint = MaterialTheme.colorScheme.error,
                    contentDescription = null,
                    modifier = Modifier.size(ListThumbnailSize)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onFavouriteClick()
                }
        )

        DefaultListItem(
            title = stringResource(id = R.string.title_library_downloaded),
            subtitle = stringResource(id = R.string.subtitle_library_downloaded),
            thumbnailContent = {
                Icon(
                    imageVector = IconApp.DownloadForOffline,
                    contentDescription = null,
                    modifier = Modifier.size(ListThumbnailSize)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onDownloadedClick()
                }
        )

        DefaultListItem(
            title = stringResource(id = R.string.title_library_device_songs),
            subtitle = stringResource(id = R.string.subtitle_library_device_songs),
            thumbnailContent = {
                Icon(
                    imageVector = IconApp.PhoneAndroid,
                    contentDescription = null,
                    modifier = Modifier.size(ListThumbnailSize)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onDeviceSongsClick()
                }
        )
    }
}