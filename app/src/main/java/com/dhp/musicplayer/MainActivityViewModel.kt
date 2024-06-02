package com.dhp.musicplayer

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhp.musicplayer.api.reponse.KeyResponse
import com.dhp.musicplayer.constant.ConfigApiKey
import com.dhp.musicplayer.constant.DarkThemeConfigKey
import com.dhp.musicplayer.enums.DarkThemeConfig
import com.dhp.musicplayer.enums.UiState
import com.dhp.musicplayer.extensions.toEnum
import com.dhp.musicplayer.model.UserData
import com.dhp.musicplayer.repository.MusicRepository
import com.dhp.musicplayer.utils.Logg
import com.dhp.musicplayer.utils.dataStore
import com.dhp.musicplayer.utils.get
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
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
            if (application.dataStore[ConfigApiKey] != null) return@launch
            val result = musicRepository.getKey()
            if (result.isSuccess) {
                result.getOrNull()?.result?.let { key ->
                    Logg.d("getKey: ${key}")
                    val keyString = try {
                        Json.encodeToString(KeyResponse.serializer(), key)
                    } catch (e: Exception) {
                        null
                    }
                    keyString?.let { string ->
                        application.dataStore.edit {
                            it[ConfigApiKey] = string
                        }
                    }

                }
            }
        }
    }

    val uiState: StateFlow<UiState<UserData>> =
        application.dataStore.data.distinctUntilChanged()
            .map { dataStore -> dataStore[DarkThemeConfigKey].toEnum(DarkThemeConfig.FOLLOW_SYSTEM) }
            .distinctUntilChanged().map { darkThemeConfig ->
                UiState.Success(UserData(darkThemeConfig = darkThemeConfig))
            }
            .stateIn(
                scope = viewModelScope,
                initialValue = UiState.Loading,
                started = SharingStarted.WhileSubscribed(5_000),
            )
}
