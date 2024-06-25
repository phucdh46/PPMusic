package com.dhp.musicplayer.feature.settings.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dhp.musicplayer.core.designsystem.R
import com.dhp.musicplayer.feature.settings.items.SettingTextItem
import com.dhp.musicplayer.feature.settings.items.SettingTopTitleItem

@Composable
internal fun SettingOthersSection(
    modifier: Modifier = Modifier,
    versionName: String,
) {

    Column(modifier) {
        SettingTopTitleItem(
            modifier = Modifier.fillMaxWidth(),
            text = R.string.setting_top_others,
        )

        SettingTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.setting_top_others_version),
            description = versionName,
            onClick = { /* do nothing */ },
        )
    }
}