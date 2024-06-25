package com.dhp.musicplayer.feature.settings.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dhp.musicplayer.core.designsystem.R
import com.dhp.musicplayer.core.designsystem.icon.IconApp
import com.dhp.musicplayer.feature.settings.dialog.CacheSettingDialog
import com.dhp.musicplayer.feature.settings.dialog.ConfirmDeleteCacheSettingDialog

@Composable
internal fun SettingCacheItem(
    modifier: Modifier = Modifier,
    title: String,
    description: String?,
    values: List<Int>? = null,
    valueText: (@Composable (Int) -> String)? = null,
    selectedValue: Int? = null,
    onValueSelected: ((Int) -> Unit)? = null,
    onConfirmCleanCache: (() -> Unit)? = null,
    showButtonClear: Boolean = true
) {

    var showSettingDialog by remember {
        mutableStateOf(false)
    }
    var showConfirmDeleteDialog by remember {
        mutableStateOf(false)
    }

    if (showSettingDialog && values != null && valueText != null && selectedValue != null && onValueSelected != null) {
        CacheSettingDialog(
            title = title,
            onDismiss = { showSettingDialog = false },
            values = values,
            valueText = valueText,
            selectedValue = selectedValue,
            onValueSelected = onValueSelected
        )
    }

    if (showConfirmDeleteDialog) {
        ConfirmDeleteCacheSettingDialog(
            title = title,
            onDismiss = { showConfirmDeleteDialog = false },
            onConfirm = onConfirmCleanCache,
            text = stringResource(id = R.string.setting_text_confirm_clean_cache_dialog)
        )
    }

    Row(
        modifier = modifier
            .clickable(
                onClick = { showSettingDialog = true },
            )
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(
                space = 4.dp,
                alignment = Alignment.CenterVertically,
            ),
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (description != null) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (showButtonClear) {
            Icon(imageVector = IconApp.CleaningServices, contentDescription = "delete",
                modifier = Modifier.clickable {
                    showConfirmDeleteDialog = true
                })
        }
    }
}
