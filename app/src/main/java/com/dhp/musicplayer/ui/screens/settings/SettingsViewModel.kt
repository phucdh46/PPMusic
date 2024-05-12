package com.dhp.musicplayer.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhp.musicplayer.enums.DarkThemeConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class SettingsViewModel @Inject constructor(
//    private val userDataRepository: UserDataRepository,
) : ViewModel() {

    val settingsUiState: StateFlow<SettingsUiState> = flowOf(SettingsUiState.Loading)
//        userDataRepository.userData
//            .map { userData ->
//                SettingsUiState.Success(
//                    settings = UserEditableSettings(
//                        darkThemeConfig = userData.darkThemeConfig,
//                    ),
//                )
//            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
                initialValue = SettingsUiState.Loading,
            )

    fun updateDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        viewModelScope.launch {
//            userDataRepository.setDarkThemeConfig(darkThemeConfig)
        }
    }
}

data class UserEditableSettings(
    val darkThemeConfig: DarkThemeConfig,
)

sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data class Success(val settings: UserEditableSettings) : SettingsUiState
}

//inline fun <VM : ViewModel> viewModelFactory(crossinline f: () -> VM) =
//    object : ViewModelProvider.Factory {
//        override fun <T : ViewModel> create(aClass: Class<T>):T = f() as T
//    }

