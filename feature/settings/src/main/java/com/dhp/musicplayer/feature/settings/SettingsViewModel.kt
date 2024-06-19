package com.dhp.musicplayer.feature.settings

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhp.musicplayer.core.common.enums.UiState
import com.dhp.musicplayer.core.common.extensions.toEnum
import com.dhp.musicplayer.core.model.settings.DarkThemeConfig
import com.dhp.musicplayer.data.datastore.DarkThemeConfigKey
import com.dhp.musicplayer.data.datastore.dataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import com.dhp.musicplayer.data.datastore.edit

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
//            application.dataStore.edit { preferences ->
//                preferences[DarkThemeConfigKey] = darkThemeConfig.name
//            }
            application.dataStore.edit(application, DarkThemeConfigKey, darkThemeConfig.name)
        }
    }
}

data class UserEditableSettings(
    val darkThemeConfig: DarkThemeConfig,
)