package com.dhp.musicplayer.core.common.extensions

import android.text.format.DateUtils
import kotlin.math.absoluteValue
import kotlin.math.max

inline fun <reified T : Enum<T>> String?.toEnum(defaultValue: T): T =
    if (this == null) defaultValue
    else try {
        enumValueOf(this)
    } catch (e: IllegalArgumentException) {
        defaultValue
    }

fun String?.thumbnail(size: Int): String? {
    return when {
        this?.startsWith("https://lh3.googleusercontent.com") == true -> "$this-w$size-h$size"
        this?.startsWith("https://yt3.ggpht.com") == true -> "$this-w$size-h$size-s$size"
        else -> this
    }
}

fun formatAsDuration(millis: Long) = DateUtils.formatElapsedTime(millis / 1000).removePrefix("0")

fun formatFileSize(sizeBytes: Long): String {
    val prefix = if (sizeBytes < 0) "-" else ""
    var result: Long = sizeBytes.absoluteValue
    var suffix = "B"
    if (result > 900) {
        suffix = "KB"
        result /= 1024
    }
    if (result > 900) {
        suffix = "MB"
        result /= 1024
    }
    if (result > 900) {
        suffix = "GB"
        result /= 1024
    }
    if (result > 900) {
        suffix = "TB"
        result /= 1024
    }
    if (result > 900) {
        suffix = "PB"
        result /= 1024
    }
    return "$prefix$result $suffix"
}

fun calculatorPercentCache(usedCache: Long, totalCache: Long): Long {
    return if (usedCache == 0L || totalCache == 0L) 0
    else max(1, usedCache * 100 / totalCache)
}

fun joinByBullet(vararg str: String?) =
    str.filterNot {
        it.isNullOrEmpty()
    }.joinToString(separator = " • ")
