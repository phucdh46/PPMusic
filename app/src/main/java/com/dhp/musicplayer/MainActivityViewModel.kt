package com.dhp.musicplayer

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhp.musicplayer.core.common.enums.UiState
import com.dhp.musicplayer.core.common.extensions.toEnum
import com.dhp.musicplayer.core.common.model.isSuccess
import com.dhp.musicplayer.core.datastore.ApiConfigKey
import com.dhp.musicplayer.core.datastore.DarkThemeConfigKey
import com.dhp.musicplayer.core.datastore.dataStore
import com.dhp.musicplayer.core.datastore.get
import com.dhp.musicplayer.core.domain.user_case.GetApiKeyUseCase
import com.dhp.musicplayer.core.model.settings.ApiKey
import com.dhp.musicplayer.core.model.settings.DarkThemeConfig
import com.dhp.musicplayer.core.model.settings.UserData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val getApiKeyUseCase: GetApiKeyUseCase,
) : ViewModel() {

    private val _getApiKeyError: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val getApiKeyError: StateFlow<Boolean> = _getApiKeyError

    init {
        getApiKey()
    }

    fun getApiKey() {
        viewModelScope.launch(Dispatchers.IO) {
            if (application.dataStore[ApiConfigKey] != null) return@launch
            val result = getApiKeyUseCase()
            if (result?.isSuccess() == true) {
                result.result?.let { key ->
                    val keyString = try {
                        Json.encodeToString(ApiKey.serializer(), key)
                    } catch (e: Exception) {
                        null
                    }
                    keyString?.let { string ->
                        application.dataStore.edit {
                            it[ApiConfigKey] = string
                        }
                    }
                    _getApiKeyError.value = false
                }
            } else {
                _getApiKeyError.value = true
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
