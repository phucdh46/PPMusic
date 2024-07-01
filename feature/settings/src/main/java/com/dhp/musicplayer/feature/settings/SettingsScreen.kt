package com.dhp.musicplayer.feature.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.dhp.musicplayer.core.common.enums.UiState
import com.dhp.musicplayer.core.datastore.DarkThemeConfigKey
import com.dhp.musicplayer.core.datastore.DynamicThemeKey
import com.dhp.musicplayer.core.model.settings.DarkThemeConfig
import com.dhp.musicplayer.core.ui.LocalWindowInsets
import com.dhp.musicplayer.core.ui.common.rememberEnumPreference
import com.dhp.musicplayer.core.ui.common.rememberPreference
import com.dhp.musicplayer.feature.settings.dialog.AppThemeSettingDialog
import com.dhp.musicplayer.feature.settings.section.SettingAudioSection
import com.dhp.musicplayer.feature.settings.section.SettingCacheSection
import com.dhp.musicplayer.feature.settings.section.SettingOthersSection
import com.dhp.musicplayer.feature.settings.section.SettingPlayerSection
import com.dhp.musicplayer.feature.settings.section.SettingThemeSection

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settingsUiState by viewModel.settingsUiState.collectAsState()
    Box(modifier = Modifier.fillMaxSize()) {
        when (settingsUiState) {
            is UiState.Success -> {
                SettingsScreen(userEditableSettings = (settingsUiState as UiState.Success<UserEditableSettings>).data)
            }

            else -> {}
        }
    }
}

@Composable
fun SettingsScreen(
    userEditableSettings: UserEditableSettings
) {
    val context = LocalContext.current
    val versionName = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    } catch (_: Exception) {
        null
    }

    val (dynamicTheme, onDynamicThemeChange) = rememberPreference(
        DynamicThemeKey,
        defaultValue = true
    )

    val (appTheme, onAppThemeChange) = rememberEnumPreference(
        DarkThemeConfigKey,
        defaultValue = DarkThemeConfig.FOLLOW_SYSTEM
    )

    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }

    if (showSettingsDialog) {
        AppThemeSettingDialog(
            onDismiss = { showSettingsDialog = false },
            darkThemeConfig = appTheme,
            onChangeDarkThemeConfig = onAppThemeChange
        )
    }

    LazyColumn(
        state = rememberLazyListState(),
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(LocalWindowInsets.current),
    ) {
        item {
            SettingThemeSection(
                modifier = Modifier.fillMaxWidth(),
                darkThemeConfig = appTheme,
                onClickAppTheme = { showSettingsDialog = true },
                dynamicColor = dynamicTheme,
                onDynamicColorChange = onDynamicThemeChange,
            )

            SettingPlayerSection(
                modifier = Modifier.fillMaxWidth(),
            )

            SettingAudioSection(
                modifier = Modifier.fillMaxWidth(),
            )

            SettingCacheSection(
                modifier = Modifier.fillMaxWidth(),
            )

            SettingOthersSection(
                modifier = Modifier.fillMaxWidth(),
                versionName = versionName.orEmpty(),
            )
        }
    }
}