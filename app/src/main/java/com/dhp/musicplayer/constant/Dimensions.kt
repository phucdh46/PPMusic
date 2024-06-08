package com.dhp.musicplayer.constant

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val NavigationBarHeight = 80.dp
val MiniPlayerHeight = 64.dp
val TopBarHeight = 56.dp

val ThumbnailCornerRadius = 6.dp
val AlbumThumbnailSizeDp = 108.dp
val ArtistThumbnailSizeDp = 92.dp

val NavigationBarAnimationSpec = spring<Dp>(stiffness = Spring.StiffnessMediumLow)
val QueuePeekHeight = 64.dp

val ListItemHeight = 64.dp
val PlayListItemHeight = 84.dp
val ListThumbnailSize = 48.dp
val GridThumbnailHeight = 128.dp
val MoodAndGenresButtonHeight = 48.dp


@Suppress("ClassName")
object Dimensions {
    val itemsVerticalPadding = 8.dp

    object thumbnails {
        val album = 128.dp
        val artist = 192.dp
        val song = 54.dp
        val playlist = album

        object player {
            val song: Dp
                @Composable
                get() = with(LocalConfiguration.current) {
                    minOf(screenHeightDp, screenWidthDp)
                }.dp
        }
    }
}

inline val Dp.px: Int
    @Composable
    inline get() = with(LocalDensity.current) { roundToPx() }