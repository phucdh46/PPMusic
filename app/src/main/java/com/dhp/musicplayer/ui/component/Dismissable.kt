package com.dhp.musicplayer.ui.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dismissable(
    onDismiss: () -> Unit,

    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(confirmValueChange = {
        if (it != SwipeToDismissBoxValue.Settled) {
            onDismiss.invoke()
        }
        true
    })
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {},
        content = { content() }
    )
}