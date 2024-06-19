package com.dhp.musicplayer.core.designsystem.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TextTitle(
    modifier: Modifier = Modifier,
    text: String
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary),
        modifier = modifier.padding(8.dp)
    )
}