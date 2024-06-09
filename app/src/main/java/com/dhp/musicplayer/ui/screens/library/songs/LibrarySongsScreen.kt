package com.dhp.musicplayer.ui.screens.library.songs

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
import com.dhp.musicplayer.R
import com.dhp.musicplayer.constant.ListThumbnailSize
import com.dhp.musicplayer.enums.LibrarySongsDetailType
import com.dhp.musicplayer.ui.AppState
import com.dhp.musicplayer.ui.IconApp
import com.dhp.musicplayer.ui.items.DefaultListItem

@Composable
fun LibrarySongsScreen(
    appState: AppState,
) {
    LibrarySongsScreen(
        onFavouriteClick = {
            appState.navController.navigateToLibrarySongsDetail(
                LibrarySongsDetailType.FAVOURITE.name
            )
        },
        onDownloadedClick = {
            appState.navController.navigateToLibrarySongsDetail(
                LibrarySongsDetailType.DOWNLOADED.name
            )
        },
        onDeviceSongsClick = {
            appState.navController.navigateToLibrarySongsDetail(
                LibrarySongsDetailType.DEVICE_SONGS.name
            )
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
