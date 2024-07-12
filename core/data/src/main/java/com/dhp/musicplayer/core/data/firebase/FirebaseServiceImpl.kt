package com.dhp.musicplayer.core.data.firebase

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseServiceImpl @Inject constructor() : FirebaseService {
    private val remoteConfig
        get() = Firebase.remoteConfig

    init {
        val configSettings = remoteConfigSettings { minimumFetchIntervalInSeconds = 3600 }
        remoteConfig.setConfigSettingsAsync(configSettings)
    }

    override suspend fun fetchConfiguration(): Boolean {
        return remoteConfig.fetchAndActivate().await()
    }

    override fun getRewardedAdUnitId(): String {
        return remoteConfig.getValue(REWARDED_AD_UNIT_ID).asString()
    }

    companion object {
        private const val REWARDED_AD_UNIT_ID = "RewardedAdUnitId"
    }
}