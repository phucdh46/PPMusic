package com.dhp.musicplayer.feature.player

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.dhp.musicplayer.core.designsystem.R
import com.dhp.musicplayer.core.designsystem.component.BottomSheet
import com.dhp.musicplayer.core.designsystem.component.BottomSheetState
import com.dhp.musicplayer.core.designsystem.theme.bold
import com.dhp.musicplayer.core.designsystem.theme.center

@OptIn(UnstableApi::class)
@Composable
fun PlayerQueue(
    state: BottomSheetState,
    playerBottomSheetState: BottomSheetState,
    modifier: Modifier = Modifier,
) {
    val containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(12.dp)

    BottomSheet(
        state = state,
        modifier = modifier,
        backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(NavigationBarDefaults.Elevation),
        collapsedContent = {
            Box(
                modifier = Modifier
                    .drawBehind { drawRect(containerColor) }
                    .fillMaxSize()
                    .navigationBarsPadding()
            ) {
                Text(
                    text = stringResource(R.string.player_queue_text),
                    style = MaterialTheme.typography.bodyLarge.bold().center(),
                    modifier = Modifier
                        .align(Alignment.Center)
                )
            }
        }
    ) {
        Queue(onClickDismiss = {
            state.collapseSoft()
        })
    }
}
