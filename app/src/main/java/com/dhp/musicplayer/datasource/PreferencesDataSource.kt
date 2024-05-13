package com.dhp.musicplayer.datasource

import androidx.datastore.core.DataStore
import com.dhp.musicplayer.UserPreferences
import com.dhp.musicplayer.UserPreferences.DarkThemeConfigProto
import com.dhp.musicplayer.UserPreferences.RepeatModeProto
import com.dhp.musicplayer.copy
import com.dhp.musicplayer.enums.DarkThemeConfig
import com.dhp.musicplayer.enums.RepeatMode
import com.dhp.musicplayer.model.UserData
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PreferencesDataSource @Inject constructor(
    private val userPreferences: DataStore<UserPreferences>,
) {
    val userData = userPreferences.data
        .map {
            UserData(
                darkThemeConfig = when (it.darkThemeConfig) {
                    null,
                    DarkThemeConfigProto.DARK_THEME_CONFIG_UNSPECIFIED,
                    DarkThemeConfigProto.UNRECOGNIZED,
                    DarkThemeConfigProto.DARK_THEME_CONFIG_FOLLOW_SYSTEM,
                    -> DarkThemeConfig.FOLLOW_SYSTEM

                    DarkThemeConfigProto.DARK_THEME_CONFIG_LIGHT -> DarkThemeConfig.LIGHT
                    DarkThemeConfigProto.DARK_THEME_CONFIG_DARK -> DarkThemeConfig.DARK
                },
                repeatMode = when (it.repeatMode) {
                    RepeatModeProto.REPEAT_ONE -> RepeatMode.REPEAT_ONE
                    RepeatModeProto.REPEAT_ALL -> RepeatMode.REPEAT_ALL
                    else -> RepeatMode.NONE
                }
            )
        }

    suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        userPreferences.updateData {
            it.copy {
                this.darkThemeConfig = when (darkThemeConfig) {
                    DarkThemeConfig.FOLLOW_SYSTEM -> DarkThemeConfigProto.DARK_THEME_CONFIG_FOLLOW_SYSTEM
                    DarkThemeConfig.LIGHT -> DarkThemeConfigProto.DARK_THEME_CONFIG_LIGHT
                    DarkThemeConfig.DARK -> DarkThemeConfigProto.DARK_THEME_CONFIG_DARK
                }
            }
        }
    }

    suspend fun setRepeatMode(repeatMode: RepeatMode) {
        userPreferences.updateData {
            it.copy {
                this.repeatMode = when (repeatMode) {
                    RepeatMode.REPEAT_ONE -> RepeatModeProto.REPEAT_ONE
                    RepeatMode.REPEAT_ALL -> RepeatModeProto.REPEAT_ALL
                    else -> RepeatModeProto.NONE
                }
            }
        }
    }
}