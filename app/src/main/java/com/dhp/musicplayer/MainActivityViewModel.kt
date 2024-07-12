package com.dhp.musicplayer

import android.app.Application
import android.content.Intent
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhp.musicplayer.core.billing.repository.SubscriptionDataRepository
import com.dhp.musicplayer.core.common.constants.ExtraParameterEnum
import com.dhp.musicplayer.core.common.enums.UiState
import com.dhp.musicplayer.core.common.extensions.parcelable
import com.dhp.musicplayer.core.common.extensions.toEnum
import com.dhp.musicplayer.core.common.model.isSuccess
import com.dhp.musicplayer.core.data.firebase.FirebaseService
import com.dhp.musicplayer.core.datastore.ApiConfigKey
import com.dhp.musicplayer.core.datastore.DarkThemeConfigKey
import com.dhp.musicplayer.core.datastore.IsEnablePremiumModeKey
import com.dhp.musicplayer.core.datastore.IsSubscribeTopicMusicKey
import com.dhp.musicplayer.core.datastore.dataStore
import com.dhp.musicplayer.core.datastore.get
import com.dhp.musicplayer.core.domain.user_case.GetApiKeyUseCase
import com.dhp.musicplayer.core.model.music.Song
import com.dhp.musicplayer.core.model.settings.ApiKey
import com.dhp.musicplayer.core.model.settings.DarkThemeConfig
import com.dhp.musicplayer.core.model.settings.UserData
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
    private val firebaseService: FirebaseService,
    private val subscriptionDataRepository: SubscriptionDataRepository
) : ViewModel() {

    private val _getApiKeyError: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val getApiKeyError: StateFlow<Boolean> = _getApiKeyError

    private val _songFromNotification: MutableStateFlow<Song?> = MutableStateFlow(null)
    private val _isPlayerConnection: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val songFromNotification =
        _songFromNotification.combine(_isPlayerConnection) { song, playerConnection ->
            if (playerConnection) {
                song
            } else {
                null
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, null)


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

    init {
        getApiKey()
        viewModelScope.launch {
            firebaseService.fetchConfiguration()
        }
        verifiedPremiumMode()
        subscribeToTopic()
    }

    private fun subscribeToTopic() {
        viewModelScope.launch(Dispatchers.IO) {
            if (application.dataStore[IsSubscribeTopicMusicKey] == true) return@launch
            Firebase.messaging.subscribeToTopic(TOPIC_MUSIC)
                .addOnCompleteListener { task ->
                    viewModelScope.launch {
                        application.dataStore.edit {
                            it[IsSubscribeTopicMusicKey] = task.isSuccessful
                        }
                    }
                }
        }
    }

    private fun verifiedPremiumMode() {
        viewModelScope.launch(Dispatchers.IO) {
            val verified = subscriptionDataRepository.verified()
            application.dataStore.edit {
                it[IsEnablePremiumModeKey] = verified
            }
        }
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

    fun handleNewIntent(intent: Intent) {
        val song = intent.extras?.parcelable<Song>(ExtraParameterEnum.NotificationContentKey.pName)
        _songFromNotification.value = song
    }

    fun handlePlayerConnection(isConnect: Boolean) {
        _isPlayerConnection.value = isConnect
    }

    override fun onCleared() {
        subscriptionDataRepository.terminateBillingConnection()
    }

    companion object {
        const val TOPIC_MUSIC = "music"
    }
}
