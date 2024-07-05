package com.dhp.musicplayer.feature.settings

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhp.musicplayer.core.common.enums.UiState
import com.dhp.musicplayer.core.common.extensions.findActivity
import com.dhp.musicplayer.core.common.extensions.toEnum
import com.dhp.musicplayer.core.common.utils.Logg
import com.dhp.musicplayer.core.data.firebase.FirebaseService
import com.dhp.musicplayer.core.datastore.DarkThemeConfigKey
import com.dhp.musicplayer.core.datastore.dataStore
import com.dhp.musicplayer.core.datastore.edit
import com.dhp.musicplayer.core.model.settings.DarkThemeConfig
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val application: Application,
    private val firebaseService: FirebaseService
) : ViewModel() {
    private var mRewardedAd: MutableStateFlow<RewardedAd?> = MutableStateFlow(null)
    val rewardedAd: StateFlow<RewardedAd?> = mRewardedAd.asStateFlow()

    private var _isLoadingAd = MutableStateFlow(false)
    var isLoadingAd: StateFlow<Boolean> = _isLoadingAd.asStateFlow()

    val settingsUiState = application.dataStore.data.distinctUntilChanged()
        .map { dataStore -> dataStore[DarkThemeConfigKey].toEnum(DarkThemeConfig.FOLLOW_SYSTEM) }
        .distinctUntilChanged()
        .map {
            UiState.Success(
                UserEditableSettings(
                    darkThemeConfig = it,
                )
            )
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

    fun loadRewarded(context: Context) {
        _isLoadingAd.value = true
        Logg.d("TAG, init: ${firebaseService.getRewardedAdUnitId()}")
        RewardedAd.load(context,
            firebaseService.getRewardedAdUnitId(),
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Logg.d("TAG, adError: ${adError}")
                    mRewardedAd.value = null
                    _isLoadingAd.value = false

                }

                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    Logg.d("TAG, 'Ad was loaded.'")
                    _isLoadingAd.value = false
                    mRewardedAd.value = rewardedAd
                }
            })
    }

    fun showRewarded(
        context: Context,
        onAdDismissed: () -> Unit,
        onUserEarnedReward: (Int) -> Unit
    ) {
        val activity = context.findActivity()

        if (mRewardedAd.value != null && activity != null
        ) {
            mRewardedAd.value?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdFailedToShowFullScreenContent(e: com.google.android.gms.ads.AdError) {
                    mRewardedAd.value = null
                }

                override fun onAdDismissedFullScreenContent() {
                    mRewardedAd.value = null
                    onAdDismissed()
                }

            }
            Logg.d("TAG, show mRewardedAd: $activity")
            mRewardedAd.value?.show(
                activity
            ) { rewardItem -> // Handle the reward
                Logg.d("TAG, rewardItem: ${rewardItem.amount} - ${rewardItem.type}")
                mRewardedAd.value = null
                onUserEarnedReward(rewardItem.amount)
            }
        }
    }

    private fun removeRewarded() {
        mRewardedAd.value?.fullScreenContentCallback = null
        mRewardedAd.value = null
    }

    override fun onCleared() {
        removeRewarded()
        super.onCleared()
    }
}

data class UserEditableSettings(
    val darkThemeConfig: DarkThemeConfig,
)