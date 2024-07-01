package com.dhp.musicplayer.core.designsystem.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dhp.musicplayer.core.designsystem.constant.px

@Stable
class MenuState(
    isVisible: Boolean = false,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    var isVisible by mutableStateOf(isVisible)
    var content by mutableStateOf(content)

    fun show(content: @Composable ColumnScope.() -> Unit) {
        isVisible = true
        this.content = content
    }

    fun dismiss() {
        isVisible = false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetMenu(
    modifier: Modifier = Modifier,
    state: MenuState,
    background: Color = MaterialTheme.colorScheme.surfaceColorAtElevation(NavigationBarDefaults.Elevation),
) {

    val stateBottomSheet = rememberModalBottomSheetState(true)

    LaunchedEffect(state.isVisible) {
        if (!state.isVisible) stateBottomSheet.hide()
    }

    if (state.isVisible) {
        ModalBottomSheet(
            modifier = modifier,
            sheetState = stateBottomSheet,
            dragHandle = { },
            containerColor = background,
            windowInsets = WindowInsets(0, WindowInsets.systemBars
                .asPaddingValues()
                .calculateTopPadding().px, 0, 0),
            shape = RoundedCornerShape(
                topStart = 0.dp,
                topEnd = 0.dp,
            ),
            onDismissRequest = { state.dismiss() },
        ) {
            state.content(this)
        }
    }
}