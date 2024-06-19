package com.dhp.musicplayer.feature.library.songs.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.dhp.musicplayer.feature.library.songs.detail.navigation.LIBRARY_SONGS_DETAIL_TYPE_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class LibrarySongsDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val type: StateFlow<String?> =
        savedStateHandle.getStateFlow(LIBRARY_SONGS_DETAIL_TYPE_ARG, null)

}