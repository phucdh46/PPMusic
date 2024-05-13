package com.dhp.musicplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhp.musicplayer.model.UserData
import com.dhp.musicplayer.repository.MusicRepository
import com.dhp.musicplayer.repository.UserDataRepository
import com.dhp.musicplayer.utils.Logg
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    userDataRepository: UserDataRepository,
    private val musicRepository: MusicRepository,
) : ViewModel() {

    init {
        initConfig()
    }

    private fun initConfig() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = musicRepository.getKey()
            if (result.isSuccess) {
                result.getOrNull()?.result?.let { key ->
                    Logg.d( "getKey: ${key}")
                    key.saveConfig()
                }
            }
        }
    }

    val uiState: StateFlow<MainActivityUiState> =
        userDataRepository.userData.map {
            MainActivityUiState.Success(it)
        }
            .stateIn(
                scope = viewModelScope,
                initialValue = MainActivityUiState.Loading,
                started = SharingStarted.WhileSubscribed(5_000),
            )
}

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState
    data class Success(val userData: UserData) : MainActivityUiState
}