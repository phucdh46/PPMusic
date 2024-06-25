package com.dhp.musicplayer.feature.settings.utils

import android.content.Context
import com.dhp.musicplayer.core.model.settings.DarkThemeConfig
import com.dhp.musicplayer.core.designsystem.R

fun DarkThemeConfig.toString(context: Context): String {
    return context.getString(
        when (this) {
            DarkThemeConfig.LIGHT -> R.string.settings_theme_light
            DarkThemeConfig.DARK -> R.string.settings_theme_dark
            DarkThemeConfig.FOLLOW_SYSTEM -> R.string.settings_theme_system_default
        }
    )
}