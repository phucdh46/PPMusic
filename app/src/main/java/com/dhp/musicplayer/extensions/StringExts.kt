package com.dhp.musicplayer.extensions

fun String?.orNA() = when (this.isNullOrEmpty()) {
    false -> this
    else -> "N/A"
}