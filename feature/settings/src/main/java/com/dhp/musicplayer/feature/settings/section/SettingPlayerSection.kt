package com.dhp.musicplayer.feature.settings.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dhp.musicplayer.core.datastore.PersistentQueueEnableKey
import com.dhp.musicplayer.core.datastore.ResumePlaybackWhenDeviceConnectedKey
import com.dhp.musicplayer.core.designsystem.R
import com.dhp.musicplayer.core.ui.common.rememberPreference
import com.dhp.musicplayer.feature.settings.items.SettingSwitchItem
import com.dhp.musicplayer.feature.settings.items.SettingTopTitleItem

@Composable
internal fun SettingPlayerSection(
    modifier: Modifier = Modifier,
) {
    val (persistentQueueEnable, onPersistentQueueEnableChange) = rememberPreference(
        PersistentQueueEnableKey,
        defaultValue = true,
    )
    val (resumePlaybackWhenDeviceConnected, onResumePlaybackWhenDeviceConnectedChange) = rememberPreference(
        ResumePlaybackWhenDeviceConnectedKey,
        false
    )
    Column(modifier) {
        SettingTopTitleItem(
            modifier = Modifier.fillMaxWidth(),
            text = R.string.setting_player_title,
        )

        SettingSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = R.string.setting_player_persistent_title,
            description = R.string.setting_player_persistent_description,
            value = persistentQueueEnable,
            onValueChanged = onPersistentQueueEnableChange,
        )

        SettingSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = R.string.setting_player_resume_title,
            description = R.string.setting_player_resume_description,
            value = resumePlaybackWhenDeviceConnected,
            onValueChanged = onResumePlaybackWhenDeviceConnectedChange,
        )
    }
}
