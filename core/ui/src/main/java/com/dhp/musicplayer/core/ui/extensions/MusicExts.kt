package com.dhp.musicplayer.core.ui.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Size
import com.dhp.musicplayer.core.model.music.Song
import com.dhp.musicplayer.core.services.extensions.toContentUri
import com.dhp.musicplayer.core.ui.R
import com.dhp.musicplayer.data.network.innertube.Innertube
import java.io.IOException

fun Innertube.SongItem.toSong(): Song {
    return Song(
        id = key,
        title = info?.name.orEmpty(),
        artistsText = authors?.joinToString("") { it.name ?: "" },
        durationText = durationText,
        thumbnailUrl = thumbnail?.url,
    )
}

fun Song.getBitmap(context: Context): Bitmap? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        try {
            context.contentResolver.loadThumbnail(
                idLocal.toContentUri(), Size(640, 480), null)
        } catch(e: IOException) {
            null
        }
    } else {
        null
    }
}

fun drawableToBitmap(context: Context, drawable: Int = R.drawable.logo_grayscale): Bitmap {
    return BitmapFactory.decodeResource(context.resources, drawable)
}

fun getTitleTextInnertubeItem(item: Innertube.Item): String {
    return when (item) {
        is Innertube.SongItem -> item.info?.name.orEmpty()
        is Innertube.AlbumItem -> item.info?.name.orEmpty()
        is Innertube.PlaylistItem -> item.info?.name.orEmpty()
        is Innertube.ArtistItem -> item.info?.name.orEmpty()
    }
}

fun getSubTitleTextInnertubeItem(item: Innertube.Item): String {
    return when (item) {
        is Innertube.SongItem -> item.toSong().artistsText.orEmpty()
        is Innertube.AlbumItem -> item.year.orEmpty()
        is Innertube.PlaylistItem -> item.channel?.name.orEmpty()
        is Innertube.ArtistItem -> item.subscribersCountText.orEmpty()
    }
}

fun getThumbnailInnertubeItem(item: Innertube.Item): String? {
    return when (item) {
        is Innertube.SongItem -> item.thumbnail?.url
        is Innertube.AlbumItem -> item.thumbnail?.url
        is Innertube.PlaylistItem -> item.thumbnail?.url
        is Innertube.ArtistItem -> item.thumbnail?.url
    }
}