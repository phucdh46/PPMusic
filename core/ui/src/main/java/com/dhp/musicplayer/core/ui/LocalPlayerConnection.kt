package com.dhp.musicplayer.core.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.dhp.musicplayer.core.services.player.PlayerConnection

val LocalPlayerConnection =
    staticCompositionLocalOf<PlayerConnection?> { error("No PlayerConnection provided") }