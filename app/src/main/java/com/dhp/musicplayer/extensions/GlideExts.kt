package com.dhp.musicplayer.extensions

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.dhp.musicplayer.R

fun <T> RequestBuilder<T>.playlistOptions(context: Context): RequestBuilder<T> {
    val drawableDefault = ContextCompat.getDrawable(context, R.drawable.ic_playlist)
    return diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        .placeholder(drawableDefault)
        .error(drawableDefault)
}
