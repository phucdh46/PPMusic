package com.dhp.musicplayer

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhp.musicplayer.constant.DarkThemeConfigKey
import com.dhp.musicplayer.enums.DarkThemeConfig
import com.dhp.musicplayer.extensions.toEnum
import com.dhp.musicplayer.model.UserData
import com.dhp.musicplayer.repository.MusicRepository
import com.dhp.musicplayer.utils.Logg
import com.dhp.musicplayer.utils.dataStore
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
    private val application: Application,
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

    val uiState: StateFlow<UiState<UserData>> =
        application.dataStore.data.distinctUntilChanged()
            .map { dataStore -> dataStore[DarkThemeConfigKey].toEnum(DarkThemeConfig.FOLLOW_SYSTEM)}
            .distinctUntilChanged().map { darkThemeConfig ->
                UiState.Success(UserData(darkThemeConfig = darkThemeConfig))
            /*val result = musicRepository.getKey()
            if (result.isSuccess) {
                result.getOrNull()?.result?.let { key ->
                    Logg.d("getKey: ${key}")
                    key.saveConfig()
                    MainActivityUiState.Success(UserData(darkThemeConfig = darkTheme))
                }?:
                MainActivityUiState.Error
            } else {
                MainActivityUiState.Error
            }*/

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