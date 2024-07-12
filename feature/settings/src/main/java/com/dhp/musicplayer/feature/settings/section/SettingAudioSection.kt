package com.dhp.musicplayer.feature.settings.section

import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.audiofx.AudioEffect
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.media3.common.util.UnstableApi
import com.dhp.musicplayer.core.common.extensions.toast
import com.dhp.musicplayer.core.datastore.SkipSilenceKey
import com.dhp.musicplayer.core.designsystem.R
import com.dhp.musicplayer.core.ui.LocalPlayerConnection
import com.dhp.musicplayer.core.ui.common.rememberPreference
import com.dhp.musicplayer.feature.settings.items.SettingSwitchItem
import com.dhp.musicplayer.feature.settings.items.SettingTextItem
import com.dhp.musicplayer.feature.settings.items.SettingTopTitleItem

@OptIn(UnstableApi::class)
@Composable
internal fun SettingAudioSection(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val (skipSilence, onSkipSilenceChange) = rememberPreference(
        SkipSilenceKey,
        defaultValue = false,
    )

    val audioSessionId = LocalPlayerConnection.current?.player?.audioSessionId

    val activityResultLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    Column(modifier) {
        SettingTopTitleItem(
            modifier = Modifier.fillMaxWidth(),
            text = R.string.setting_audio_title,
        )

        SettingSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = R.string.setting_audio_skip_silence_title,
            description = R.string.setting_audio_skip_silence_description,
            value = skipSilence,
            onValueChanged = onSkipSilenceChange,
        )

        SettingTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(id = R.string.setting_audio_equalizer_title),
            description = stringResource(id = R.string.setting_audio_equalizer_description),
            onClick = {
                val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                    putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId)
                    putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
                    putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                }
                try {
                    activityResultLauncher.launch(intent)
                } catch (e: ActivityNotFoundException) {
                    context.toast("Couldn't find an application to equalize audio")
                }
            },
        )
    }
}
