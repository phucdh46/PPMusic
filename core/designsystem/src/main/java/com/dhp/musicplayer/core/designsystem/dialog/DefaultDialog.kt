package com.dhp.musicplayer.core.designsystem.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.dhp.musicplayer.core.designsystem.R

@Composable
fun DefaultDialog(
    onDismiss: () -> Unit,
    onConfirm: (() -> Unit)? = null,
    title: String? = null,
    isError: Boolean = false,
    confirmText: String? = null,
    cancelText: String? = null,
    disableDismiss: Boolean = false,
    content: @Composable() (() -> Unit)? = null,
) {
    val configuration = LocalConfiguration.current

    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.widthIn(max = configuration.screenWidthDp.dp - 80.dp),
        onDismissRequest = {
            if (!disableDismiss) onDismiss()
        },
        title = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title ?: stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

        },
        text = {
            content?.invoke()
        },
        confirmButton = {
            TextButton(
                enabled = !isError,
                onClick = {
                    onConfirm?.invoke()
                    onDismiss()
                },
                modifier = Modifier
                    .padding(horizontal = 8.dp),
            ) {
                Text(
                    text = confirmText
                        ?: stringResource(R.string.settings_confirm_dialog_button_text),
                    style = MaterialTheme.typography.labelMedium,
                    //color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        dismissButton = {
            if (!disableDismiss && onConfirm != null) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = cancelText ?: stringResource(R.string.settings_cancel_button_text),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground, //.copy(alpha = 0.5f),
                    )
                }
            }
        }
    )
}
