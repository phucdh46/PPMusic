package com.dhp.musicplayer.feature.settings.utils

import android.content.Context
import com.dhp.musicplayer.core.model.settings.DarkThemeConfig
import com.dhp.musicplayer.feature.settings.R

fun DarkThemeConfig.toString(context: Context): String {
    return context.getString(
        when (this) {
            DarkThemeConfig.LIGHT -> R.string.feature_settings_dark_mode_config_light
            DarkThemeConfig.DARK -> R.string.feature_settings_dark_mode_config_dark
            DarkThemeConfig.FOLLOW_SYSTEM -> R.string.feature_settings_dark_mode_config_system_default
        }
    )
}