package com.dhp.musicplayer.ui.screens.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhp.musicplayer.enums.RepeatMode
import com.dhp.musicplayer.repository.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
) : ViewModel() {
    val nowPlayingUiState: StateFlow<NowPlayingUiState> =
        userDataRepository.userData
            .map { userData ->
                NowPlayingUiState.Success(
                    settings = UserEditableNowPlaying(
                        repeatMode = userData.repeatMode,
                    ),
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
                initialValue = NowPlayingUiState.Loading,
            )

    fun updateRepeatMode(repeatMode: RepeatMode)  {
        viewModelScope.launch {
            val newRepeatMode = when(repeatMode) {
                RepeatMode.NONE -> RepeatMode.REPEAT_ONE
                RepeatMode.REPEAT_ONE -> RepeatMode.REPEAT_ALL
                RepeatMode.REPEAT_ALL -> RepeatMode.NONE
            }
            userDataRepository.setRepeatMode(newRepeatMode)
        }
    }
}

data class UserEditableNowPlaying(
    val repeatMode: RepeatMode,
)

sealed interface NowPlayingUiState {
    data object Loading : NowPlayingUiState
    data class Success(val settings: UserEditableNowPlaying) : NowPlayingUiState
}