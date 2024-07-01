package com.dhp.musicplayer.feature.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@androidx.media3.common.util.UnstableApi
@Composable
fun PlaybackError(
    isDisplayed: Boolean,
    messageProvider: () -> String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {

    Box {
        AnimatedVisibility(
            visible = isDisplayed,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Spacer(
                modifier = modifier
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                onDismiss()
                            }
                        )
                    }
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.error)
            )
        }

        AnimatedVisibility(
            visible = isDisplayed,
            enter = slideInVertically { -it },
            exit = slideOutVertically { -it },
            modifier = Modifier
                .align(Alignment.Center)
        ) {
            Text(
                text = remember { messageProvider() },
                color = MaterialTheme.colorScheme.onError,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(all = 8.dp)
                    .fillMaxWidth()
            )
        }
    }
}
