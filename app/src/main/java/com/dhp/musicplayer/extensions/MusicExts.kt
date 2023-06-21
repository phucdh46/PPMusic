package com.dhp.musicplayer.extensions

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore


fun Long.toContentUri(): Uri = ContentUris.withAppendedId(
    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
    this
)
