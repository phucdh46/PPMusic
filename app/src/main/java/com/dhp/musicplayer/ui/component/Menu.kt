package com.dhp.musicplayer.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
inline fun Menu(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxWidth()
            .padding(top = 2.dp)
            .padding(vertical = 8.dp)
            .navigationBarsPadding(),
        content = content
    )
}

@Composable
fun MenuEntry(
    imageVector: ImageVector,
    text: String,
    onClick: () -> Unit,
    secondaryText: String? = null,
    enabled: Boolean = true,
    trailingContent: (@Composable () -> Unit)? = null,
    icon: (@Composable RowScope.() -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier
            .clickable(enabled = enabled, onClick = onClick)
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.4f)
            .padding(horizontal = 24.dp)
    ) {
        icon?.let { it() } ?: Icon(
            imageVector = imageVector,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(15.dp)
        )

        Column(
            modifier = Modifier
                .padding(vertical = 16.dp)
                .weight(1f)
        ) {
            Text(
                text = text,
                style = typography.bodyMedium
            )

            secondaryText?.let { secondaryText ->
                Text(
                    text = secondaryText,
                    style = typography.bodyMedium
                )
            }
        }

        trailingContent?.invoke()
    }
}