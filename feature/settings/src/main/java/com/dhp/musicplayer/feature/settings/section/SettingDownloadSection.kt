package com.dhp.musicplayer.feature.settings.section

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.media3.common.util.UnstableApi
import com.dhp.musicplayer.core.datastore.IsEnablePremiumModeKey
import com.dhp.musicplayer.core.designsystem.R
import com.dhp.musicplayer.core.ui.common.rememberPreference
import com.dhp.musicplayer.feature.settings.items.SettingTextItem
import com.dhp.musicplayer.feature.settings.items.SettingTopTitleItem

@OptIn(UnstableApi::class)
@Composable
internal fun SettingDownloadSection(
    modifier: Modifier = Modifier,
    onWatchAdClick: () -> Unit,
    maxDownloadLimit: Int
) {
    val (isEnablePremiumMode, _) = rememberPreference(
        IsEnablePremiumModeKey,
        defaultValue = false
    )

    val totalDownload =
        if (isEnablePremiumMode) stringResource(R.string.setting_download_unlimited) else maxDownloadLimit.toString()
    Column(modifier) {
        SettingTopTitleItem(
            modifier = Modifier.fillMaxWidth(),
            text = R.string.setting_download_title,
        )

        SettingTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(
                id = R.string.setting_download_limit_title
//                ,maxDownloadLimit.toString()
            ),
            subTitle = totalDownload,
            description = stringResource(id = R.string.setting_download_limit_description),
            onClick = onWatchAdClick,
        )
    }
}
