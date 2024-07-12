package com.dhp.musicplayer.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TextIconButton(
    modifier: Modifier = Modifier,
    text: String,
    imageVector: ImageVector,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(modifier = modifier) {
        Button(
            onClick = onClick,
            enabled = enabled,
            contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Icon(
                imageVector = imageVector, contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Text(text = text)
        }
    }
}

@Composable
fun DebouncedIconButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    debounceDuration: Long = 500L, // Duration in milliseconds
    content: @Composable () -> Unit
) {
    var isClickable by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    androidx.compose.material3.IconButton(
        modifier = modifier,
        onClick = {
            if (isClickable) {
                onClick()
                isClickable = false
                coroutineScope.launch {
                    delay(debounceDuration)
                    isClickable = true
                }
            }
        },
        enabled = isClickable // Disable the button if not clickable
    ) {
        content()
    }
}