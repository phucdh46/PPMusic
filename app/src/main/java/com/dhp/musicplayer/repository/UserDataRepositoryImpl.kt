package com.dhp.musicplayer.repository

import com.dhp.musicplayer.datasource.PreferencesDataSource
import com.dhp.musicplayer.enums.DarkThemeConfig
import com.dhp.musicplayer.model.UserData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal class UserDataRepositoryImpl
@Inject constructor(
    private val preferencesDataSource: PreferencesDataSource,
)
    : UserDataRepository {
    override val userData: Flow<UserData> = preferencesDataSource.userData

    override suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        preferencesDataSource.setDarkThemeConfig(darkThemeConfig)
    }
}