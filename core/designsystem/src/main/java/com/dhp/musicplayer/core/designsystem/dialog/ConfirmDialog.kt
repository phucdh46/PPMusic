package com.dhp.musicplayer.core.designsystem.dialog

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String? = null,
    message: String,
    confirmText: String? = null,
    cancelText: String? = null,
    content: @Composable (() -> Unit)? = null,
) {
    DefaultDialog(
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        title = title,
        confirmText = confirmText,
        cancelText = cancelText,
    ) {
        content?.invoke() ?: Text(
            text = message, style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}