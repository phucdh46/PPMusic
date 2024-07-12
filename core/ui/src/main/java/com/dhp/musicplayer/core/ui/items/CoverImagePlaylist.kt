package com.dhp.musicplayer.core.ui.items

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.util.fastForEachIndexed
import com.dhp.musicplayer.core.common.extensions.thumbnail
import com.dhp.musicplayer.core.designsystem.component.LoadingShimmerImageMaxSize
import com.dhp.musicplayer.core.designsystem.constant.ListThumbnailSize
import com.dhp.musicplayer.core.designsystem.constant.px
import com.dhp.musicplayer.core.designsystem.icon.IconApp
import com.dhp.musicplayer.core.model.music.PlaylistWithSongs
import com.dhp.musicplayer.core.model.music.Song
import com.dhp.musicplayer.core.ui.extensions.drawableToBitmap
import com.dhp.musicplayer.core.ui.extensions.getBitmap

@Composable
fun CoverImagePlaylist(
    playlistWithSongs: PlaylistWithSongs,
    size: Dp = ListThumbnailSize,
) {
    val sizeSong = playlistWithSongs.songs.size
    when {
        sizeSong == 0 -> {
            Icon(
                imageVector = IconApp.PlaylistPlay,
                contentDescription = null,
                modifier = Modifier.size(size)
            )
        }

        sizeSong >= 4 -> {
            Box(
                modifier = Modifier.size(size),
            ) {
                listOf(
                    Alignment.TopStart,
                    Alignment.TopEnd,
                    Alignment.BottomStart,
                    Alignment.BottomEnd
                ).fastForEachIndexed { index, alignment ->
                    playlistWithSongs.songs.getOrNull(index)?.let { song ->
                        CoverImageSongOnOrOffline(
                            song = song,
                            size = size / 2,
                            modifier = Modifier.align(alignment)
                        )

                    }
                }
            }
        }

        else -> {
            val song = playlistWithSongs.songs[0]
            CoverImageSongOnOrOffline(
                song = song,
                size = size
            )
        }
    }
}

@Composable
fun CoverImageSongOnOrOffline(
    modifier: Modifier = Modifier,
    song: Song?,
    size: Dp
) {
    if (song?.isOffline == true) {
        Image(
            bitmap = (song.getBitmap(LocalContext.current)
                ?: drawableToBitmap(LocalContext.current)).asImageBitmap(),
            contentDescription = null,
            modifier = modifier.size(size)
        )
    } else {
        LoadingShimmerImageMaxSize(
            thumbnailUrl = song?.thumbnailUrl.thumbnail(size.px),
            modifier = modifier
                .size(size),
        )
    }
}