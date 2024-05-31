package com.dhp.musicplayer.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dhp.musicplayer.ui.IconApp

@Composable
fun PlaylistMenu(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onEditPlaylist: (() -> Unit),
    onDeletePlaylist: (() -> Unit),
) {
    Menu(
        modifier = modifier
            .padding(bottom = 16.dp)
    ) {
        MenuEntry(
            imageVector = IconApp.Edit,
            text = "Edit playlist name",
            onClick = {
                onDismiss()
                onEditPlaylist()
            }
        )
        MenuEntry(
            imageVector = IconApp.Delete,
            text = "Delete playlist",
            onClick = {
                onDismiss()
                onDeletePlaylist()
            }
        )
    }
}