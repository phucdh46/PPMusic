package com.dhp.musicplayer.utils

import androidx.compose.material3.SnackbarDuration
import com.dhp.musicplayer.R
import com.dhp.musicplayer.ui.AppState
import com.dhp.musicplayer.feature.artist.list_songs.navigation.LIST_SONGS_ROUTE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

fun getAppBarTitle(route: String?): Int? {
    return when (route) {
        LIST_SONGS_ROUTE -> R.string.list_songs_title
        else -> null
    }
}

fun AppState.showSnackBar(
    message: String,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Main) + Job(),
    duration: SnackbarDuration = SnackbarDuration.Short
) {
    scope.launch {
        snackBarHostState.showSnackbar(
            message = message,
            duration = duration
        )
    }
}
