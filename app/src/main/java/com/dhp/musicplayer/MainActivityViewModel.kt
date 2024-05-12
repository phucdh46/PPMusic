package com.dhp.musicplayer

//import com.dhp.musicplayer.api.ApiMusicService
//import com.dhp.musicplayer.api.reponse.isSuccess
//import com.dhp.musicplayer.repository.MusicRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhp.musicplayer.model.UserData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
//    userDataRepository: UserDataRepository,
//    private val musicRepository: MusicRepository,
) : ViewModel() {

    init {
        initConfig()
    }

    private fun initConfig() {
        viewModelScope.launch(Dispatchers.IO) {
//            val result = musicRepository.getKey()
//            if (result.isSuccess) {
//                result.getOrNull()?.result?.let { key ->
//                    Log.d("DHP","getKey: ${key}")
//                    key.saveConfig()
//                }
//            }
        }
    }

    val uiState: StateFlow<MainActivityUiState> = flowOf(MainActivityUiState.Loading)
//        userDataRepository.userData.map {
//        MainActivityUiState.Success(it)
//    }
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