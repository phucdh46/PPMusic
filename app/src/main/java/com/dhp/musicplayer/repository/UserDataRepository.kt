package com.dhp.musicplayer.repository

import com.dhp.musicplayer.enums.DarkThemeConfig
import com.dhp.musicplayer.enums.RepeatModes
import com.dhp.musicplayer.model.UserData
import kotlinx.coroutines.flow.Flow

interface UserDataRepository {

    val userData: Flow<UserData>

    suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig)

    suspend fun setRepeatMode(repeatMode: RepeatModes)
}