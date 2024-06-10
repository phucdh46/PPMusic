package com.dhp.musicplayer.ui.screens.library.songs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dhp.musicplayer.R
import com.dhp.musicplayer.enums.LibrarySongsDetailType
import com.dhp.musicplayer.extensions.toEnum
import com.dhp.musicplayer.ui.AppState
import com.dhp.musicplayer.ui.component.TopAppBarDetailScreen
import com.dhp.musicplayer.ui.screens.library.songs.device_songs.DeviceSongsScreen
import com.dhp.musicplayer.ui.screens.library.songs.downloaded.DownloadSongsScreen
import com.dhp.musicplayer.ui.screens.library.songs.favourites.FavouritesScreen
import com.dhp.musicplayer.utils.Logg

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibrarySongsDetailScreen(
    appState: AppState,
    viewModel: LibrarySongsDetailViewModel = hiltViewModel()
) {
    val type by viewModel.type.collectAsState()
    val enum = type.toEnum(LibrarySongsDetailType.FAVOURITE)
    Logg.d("LibrarySongsDetailScreen: enum $enum")
    type ?: return
    var title by remember {
        mutableStateOf("")
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        when (type.toEnum(LibrarySongsDetailType.FAVOURITE)) {
            LibrarySongsDetailType.FAVOURITE -> {
                title = stringResource(id = R.string.title_library_favourites)
                FavouritesScreen(appState = appState)
            }

            LibrarySongsDetailType.DOWNLOADED -> {
                title = stringResource(id = R.string.title_library_downloaded)
                DownloadSongsScreen(appState = appState)
            }

            LibrarySongsDetailType.DEVICE_SONGS -> {
                title = stringResource(id = R.string.title_library_device_songs)
                DeviceSongsScreen(appState = appState)
            }

        }

        TopAppBarDetailScreen(
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)

                )
            },
            onBackClick = { appState.navController.navigateUp() },
        )
    }

}