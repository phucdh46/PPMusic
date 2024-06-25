package com.dhp.musicplayer.feature.settings.dialog

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dhp.musicplayer.feature.settings.R

@Composable
fun CacheSettingDialog(
    onDismiss: () -> Unit,
    title: String?,
    values: List<Int>,
    valueText: @Composable (Int) -> String,
    selectedValue: Int,
    onValueSelected: (Int) -> Unit,
) {
    SettingsDialog(
        onDismiss = onDismiss,
        title = title,
        text = {
            HorizontalDivider()
            LazyColumn {
                item {
                    SettingsDialogSectionTitle(text = stringResource(R.string.setting_cache_title_dialog))
                }
                items(values) { value ->
                    SettingsDialogThemeChooserRow(
                        text = valueText(value),
                        selected = value == selectedValue,
                        onClick = { onValueSelected(value) },
                    )
                }
            }
        }
    )
}

@Composable
fun ConfirmDeleteCacheSettingDialog(
    title: String?,
    onDismiss: () -> Unit,
    onConfirm: (() -> Unit)? = null,
    text: String
) {
    SettingsDialog(
        title = title,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        text = {
            Text(text = text, style = MaterialTheme.typography.bodyMedium)
        },
    )
}
