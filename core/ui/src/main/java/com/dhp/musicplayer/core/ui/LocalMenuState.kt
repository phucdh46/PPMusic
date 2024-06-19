package com.dhp.musicplayer.core.ui

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dhp.musicplayer.core.designsystem.component.MenuState

val LocalMenuState = compositionLocalOf { MenuState() }
