package com.dhp.musicplayer.core.designsystem.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dhp.musicplayer.core.designsystem.theme.bold

@Composable
fun TextTitle(
    modifier: Modifier = Modifier,
    text: String
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.bold(),
        modifier = modifier.padding(8.dp)
    )
}