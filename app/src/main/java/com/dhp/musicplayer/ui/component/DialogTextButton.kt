package com.dhp.musicplayer.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DialogTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    primary: Boolean = false,
) {

    val (textColor, primaryColor) = when {
        !enabled -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f) to Color.Transparent
        primary -> MaterialTheme.colorScheme.onPrimaryContainer to MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.onPrimaryContainer to Color.Transparent
    }

    Text(
        text = text,
        style = typography.bodyMedium.copy(color = textColor),
        modifier = modifier
            .clip(RoundedCornerShape(36.dp))
            .background(primaryColor)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp)
    )
}
