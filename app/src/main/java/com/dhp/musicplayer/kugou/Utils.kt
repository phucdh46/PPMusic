package com.dhp.musicplayer.kugou

import kotlin.coroutines.cancellation.CancellationException

internal fun <T> Result<T>.recoverIfCancelled(): Result<T>? {
    return when (exceptionOrNull()) {
        is CancellationException -> null
        else -> this
    }
}

fun findCurrentLineIndex(lines: List<Pair<Long, String>>, position: Long): Int {
    for (index in lines.indices) {
        if (lines[index].first >= position + animateScrollDuration) {
            return index - 1
        }
    }
    return lines.lastIndex
}

const val animateScrollDuration = 300L