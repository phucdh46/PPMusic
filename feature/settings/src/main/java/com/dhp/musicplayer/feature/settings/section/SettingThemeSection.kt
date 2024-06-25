package com.dhp.musicplayer.feature.settings.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.dhp.musicplayer.core.model.settings.DarkThemeConfig
import com.dhp.musicplayer.feature.settings.R
import com.dhp.musicplayer.feature.settings.items.SettingSwitchItem
import com.dhp.musicplayer.feature.settings.items.SettingTextItem
import com.dhp.musicplayer.feature.settings.items.SettingTopTitleItem
import com.dhp.musicplayer.feature.settings.utils.toString

@Composable
internal fun SettingThemeSection(
    darkThemeConfig: DarkThemeConfig,
    onClickAppTheme: () -> Unit,
    dynamicColor: Boolean,
    onDynamicColorChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Column(modifier) {
        SettingTopTitleItem(
            modifier = Modifier.fillMaxWidth(),
            text = R.string.setting_top_theme,
        )

        SettingTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(id = R.string.setting_top_theme_app),
            description = darkThemeConfig.toString(context),
            onClick = onClickAppTheme,
        )

        SettingSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = R.string.setting_dynamic_theme_title,
            description = R.string.setting_dynamic_theme_description,
            value = dynamicColor,
            onValueChanged = onDynamicColorChange,
        )
    }
}
