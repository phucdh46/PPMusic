package com.dhp.musicplayer.core.ui.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Size
import com.dhp.musicplayer.core.common.extensions.joinByBullet
import com.dhp.musicplayer.core.model.music.Album
import com.dhp.musicplayer.core.model.music.Artist
import com.dhp.musicplayer.core.model.music.Music
import com.dhp.musicplayer.core.model.music.Playlist
import com.dhp.musicplayer.core.model.music.Song
import com.dhp.musicplayer.core.common.extensions.toContentUri
import com.dhp.musicplayer.core.designsystem.R
import java.io.IOException

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

fun getThumbnail(item : Music) :String? {
    return when(item) {
        is Song -> item.thumbnailUrl
        is Album -> item.thumbnailUrl
        is Playlist -> item.thumbnailUrl
        is Artist -> item.thumbnailUrl
    }
}

fun getTitleMusic(item: Music): String {
    return when (item) {
        is Song -> item.title
        is Album -> item.title.orEmpty()
        is Playlist -> item.name
        is Artist -> item.name.orEmpty()
    }
}

fun getSubTitleMusic(item: Music): String {
    return when (item) {
        is Song -> joinByBullet(item.artistsText, item.durationText)
        is Album -> item.year.orEmpty()
        is Playlist -> item.channelName.orEmpty()
        is Artist -> item.subscribersCountText.orEmpty()
    }
}