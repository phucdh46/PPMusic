package com.dhp.musicplayer.ui.screens.settings

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhp.musicplayer.constant.DarkThemeConfigKey
import com.dhp.musicplayer.enums.DarkThemeConfig
import com.dhp.musicplayer.enums.UiState
import com.dhp.musicplayer.extensions.toEnum
import com.dhp.musicplayer.utils.dataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val application: Application,
) : ViewModel() {

    val settingsUiState = application.dataStore.data.distinctUntilChanged()
        .map { dataStore -> dataStore[DarkThemeConfigKey].toEnum(DarkThemeConfig.FOLLOW_SYSTEM) }
        .distinctUntilChanged()
        .map {
            UiState.Success(UserEditableSettings(
                darkThemeConfig = it,
            ))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
            initialValue = UiState.Loading,
        )

    fun updateDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        viewModelScope.launch {
            application.dataStore.edit { preferences ->
                preferences[DarkThemeConfigKey] = darkThemeConfig.name
            }

        }
    }
}

data class UserEditableSettings(
    val darkThemeConfig: DarkThemeConfig,
)