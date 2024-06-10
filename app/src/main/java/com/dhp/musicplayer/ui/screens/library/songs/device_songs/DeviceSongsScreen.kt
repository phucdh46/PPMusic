package com.dhp.musicplayer.ui.screens.library.songs.device_songs

import android.Manifest
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dhp.musicplayer.R
import com.dhp.musicplayer.constant.Dimensions
import com.dhp.musicplayer.enums.UiState
import com.dhp.musicplayer.extensions.asMediaItem
import com.dhp.musicplayer.extensions.isAtLeastAndroid33
import com.dhp.musicplayer.model.Song
import com.dhp.musicplayer.ui.AppState
import com.dhp.musicplayer.ui.IconApp
import com.dhp.musicplayer.ui.LocalPlayerConnection
import com.dhp.musicplayer.ui.LocalWindowInsets
import com.dhp.musicplayer.ui.component.LocalMenuState
import com.dhp.musicplayer.ui.component.MediaItemMenu
import com.dhp.musicplayer.ui.component.SongItemPlaceholder
import com.dhp.musicplayer.ui.items.DeviceSongItem
import com.dhp.musicplayer.utils.openSettingsForReadExternalStorage
import com.dhp.musicplayer.utils.showSnackBar
import com.dhp.musicplayer.utils.stringToBitMap
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DeviceSongsScreen(appState: AppState) {
    val context = LocalContext.current
    val permissionReadAudio = if (isAtLeastAndroid33) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    val readAudioPermissionState = rememberPermissionState(permissionReadAudio)

    if (readAudioPermissionState.status.isGranted) {
        DeviceSongsScreen(modifier = Modifier, appState = appState)
    } else {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val textToShow = if (readAudioPermissionState.status.shouldShowRationale) {
                stringResource(id = R.string.permission_denied)
            } else {
                stringResource(id = R.string.permission_not_available)
            }

            Text(
                textToShow,
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { readAudioPermissionState.launchPermissionRequest() },
                ) {
                    Text(text = stringResource(id = R.string.grant_permission_button))
                }
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { openSettingsForReadExternalStorage(context) },
                ) {
                    Text(text = stringResource(id = R.string.go_to_settings_button))
                }
            }
        }
    }
}

@Composable
fun DeviceSongsScreen(
    modifier: Modifier,
    appState: AppState,
    viewModel: DeviceSongsViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.getDeviceMusic()
    }
    val uiState by viewModel.uiState.observeAsState()
    when (uiState) {
        is UiState.Loading -> {
            Column(
                modifier = Modifier
                    .windowInsetsPadding(LocalWindowInsets.current)
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                repeat(7) {
                    SongItemPlaceholder()
                }
            }
        }

        is UiState.Success<List<Song>> -> {
            val songs = (uiState as UiState.Success<List<Song>>).data
            DeviceSongsScreen(
                modifier = modifier,
                appState = appState,
                songs = songs
            )
        }

        else -> {}
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun DeviceSongsScreen(
    modifier: Modifier,
    appState: AppState,
    songs: List<Song>
) {
    val lazyListState = rememberLazyListState()
    val playerConnection = LocalPlayerConnection.current
    val menuState = LocalMenuState.current
    val songsAndBitmap = songs.map { it to stringToBitMap(it.thumbnailUrl) }

    Box(
        modifier = Modifier
            .windowInsetsPadding(LocalWindowInsets.current)
            .fillMaxSize()
    ) {
        LazyColumn(
            state = lazyListState,
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = pluralStringResource(
                            R.plurals.n_song,
                            songs.size,
                            songs.size
                        ),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(Modifier.weight(1f))
                }
            }
            items(items = songsAndBitmap, key = { it.first.id }) { songsAndBitmap ->
                DeviceSongItem(
                    song = songsAndBitmap.first,
                    bitmap = songsAndBitmap.second,
                    thumbnailSizeDp = Dimensions.thumbnails.song,
                    trailingContent = {
                        Box {
                            IconButton(
                                onClick = {
                                    menuState.show {
                                        MediaItemMenu(
                                            appState = appState,
                                            onDismiss = menuState::dismiss,
                                            mediaItem = songsAndBitmap.first.asMediaItem(),
                                            onShowMessageAddSuccess = appState::showSnackBar
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = IconApp.MoreVert,
                                    contentDescription = null
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .combinedClickable(
                            onLongClick = {
                                menuState.show {
                                    MediaItemMenu(
                                        appState = appState,
                                        onDismiss = menuState::dismiss,
                                        mediaItem = songsAndBitmap.first.asMediaItem(),
                                        onShowMessageAddSuccess = appState::showSnackBar
                                    )
                                }
                            },
                            onClick = {
                                playerConnection?.playSongWithQueue(
                                    songsAndBitmap.first,
                                    songs
                                )
                            }
                        )
                        .animateItemPlacement()
                )
            }
        }
    }
}