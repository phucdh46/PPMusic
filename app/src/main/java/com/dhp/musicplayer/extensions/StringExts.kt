package com.dhp.musicplayer.extensions

fun String?.orNA() = when (this.isNullOrEmpty()) {
    false -> this
    else -> "N/A"
}

fun String?.thumbnail(size: Int): String? {
    return when {
        this?.startsWith("https://lh3.googleusercontent.com") == true -> "$this-w$size-h$size"
        this?.startsWith("https://yt3.ggpht.com") == true -> "$this-w$size-h$size-s$size"
        else -> this
    }
}

inline fun <reified T : Enum<T>> String?.toEnum(defaultValue: T): T =
    if (this == null) defaultValue
    else try {
        enumValueOf(this)
    } catch (e: IllegalArgumentException) {
        defaultValue
    }