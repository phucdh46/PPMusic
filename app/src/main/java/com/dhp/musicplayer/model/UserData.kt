package com.dhp.musicplayer.model

import com.dhp.musicplayer.enums.DarkThemeConfig
import com.dhp.musicplayer.enums.RepeatMode

data class UserData(
    val darkThemeConfig: DarkThemeConfig,
    val repeatMode: RepeatMode,
)