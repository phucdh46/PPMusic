package com.dhp.musicplayer.core.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.compositionLocalOf

val LocalWindowInsets = compositionLocalOf<WindowInsets> { error("No WindowInsets provided") }
