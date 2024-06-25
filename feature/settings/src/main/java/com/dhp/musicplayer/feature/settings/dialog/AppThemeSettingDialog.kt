package com.dhp.musicplayer.feature.settings.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dhp.musicplayer.core.model.settings.DarkThemeConfig
import com.dhp.musicplayer.feature.settings.R

@Composable
fun AppThemeSettingDialog(
    onDismiss: () -> Unit,
    darkThemeConfig: DarkThemeConfig,
    onChangeDarkThemeConfig: (darkThemeConfig: DarkThemeConfig) -> Unit,
) {
    SettingsDialog(
        onDismiss = onDismiss,
        text = {
            HorizontalDivider()
            Column(Modifier.verticalScroll(rememberScrollState())) {
                AppThemeSettingDialog(
                    darkThemeConfig = darkThemeConfig,
                    onChangeDarkThemeConfig = onChangeDarkThemeConfig,
                )
                HorizontalDivider(Modifier.padding(top = 8.dp))
            }
        }
    )
}

@Composable
private fun AppThemeSettingDialog(
    darkThemeConfig: DarkThemeConfig,
    onChangeDarkThemeConfig: (darkThemeConfig: DarkThemeConfig) -> Unit,
) {
    SettingsDialogSectionTitle(text = stringResource(R.string.feature_settings_dark_mode_preference))
    Column(Modifier.selectableGroup()) {
        SettingsDialogThemeChooserRow(
            text = stringResource(R.string.feature_settings_dark_mode_config_system_default),
            selected = darkThemeConfig == DarkThemeConfig.FOLLOW_SYSTEM,
            onClick = { onChangeDarkThemeConfig(DarkThemeConfig.FOLLOW_SYSTEM) },
        )
        SettingsDialogThemeChooserRow(
            text = stringResource(R.string.feature_settings_dark_mode_config_light),
            selected = darkThemeConfig == DarkThemeConfig.LIGHT,
            onClick = { onChangeDarkThemeConfig(DarkThemeConfig.LIGHT) },
        )
        SettingsDialogThemeChooserRow(
            text = stringResource(R.string.feature_settings_dark_mode_config_dark),
            selected = darkThemeConfig == DarkThemeConfig.DARK,
            onClick = { onChangeDarkThemeConfig(DarkThemeConfig.DARK) },
        )
    }
}
