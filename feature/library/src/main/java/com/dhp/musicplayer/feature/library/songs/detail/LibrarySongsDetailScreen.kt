package com.dhp.musicplayer.feature.library.songs.detail

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
import com.dhp.musicplayer.core.common.extensions.toEnum
import com.dhp.musicplayer.core.common.utils.Logg
import com.dhp.musicplayer.core.designsystem.component.TopAppBarDetailScreen
import com.dhp.musicplayer.core.model.settings.LibrarySongsDetailType
import com.dhp.musicplayer.feature.library.R
import com.dhp.musicplayer.feature.library.songs.device_songs.DeviceSongsScreen
import com.dhp.musicplayer.feature.library.songs.downloaded.DownloadSongsScreen
import com.dhp.musicplayer.feature.library.songs.favourites.FavouritesScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibrarySongsDetailScreen(
    viewModel: LibrarySongsDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    showSnackBar: (String) -> Unit,

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
                FavouritesScreen(showSnackBar = showSnackBar)
            }

            LibrarySongsDetailType.DOWNLOADED -> {
                title = stringResource(id = R.string.title_library_downloaded)
                DownloadSongsScreen(showSnackBar = showSnackBar)
            }

            LibrarySongsDetailType.DEVICE_SONGS -> {
                title = stringResource(id = R.string.title_library_device_songs)
                DeviceSongsScreen(
                    showMessage = showSnackBar
                )
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
            onBackClick = onBackClick,
        )
    }

}