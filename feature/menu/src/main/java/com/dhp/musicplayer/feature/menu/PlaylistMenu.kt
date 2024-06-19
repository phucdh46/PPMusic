package com.dhp.musicplayer.feature.menu

import com.dhp.musicplayer.core.designsystem.component.Menu
import com.dhp.musicplayer.core.designsystem.component.MenuEntry


import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dhp.musicplayer.core.designsystem.icon.IconApp

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
            text = stringResource(R.string.title_rename_dialog),
            onClick = {
                onDismiss()
                onEditPlaylist()
            }
        )
        MenuEntry(
            imageVector = IconApp.Delete,
            text = stringResource(R.string.title_delete_dialog),
            onClick = {
                onDismiss()
                onDeletePlaylist()
            }
        )
    }
}