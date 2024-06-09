package com.dhp.musicplayer.ui.screens.library.songs.favourites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhp.musicplayer.db.MusicDao
import com.dhp.musicplayer.enums.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FavouritesViewModel @Inject constructor(
    musicDao: MusicDao
) : ViewModel() {
    val uiState = musicDao.favorites().map {
        if (it.isEmpty()) {
            UiState.Empty
        } else {
            UiState.Success(it)
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading
        )
}