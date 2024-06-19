package com.dhp.musicplayer.core.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.dhp.musicplayer.core.services.download.DownloadUtil

val LocalDownloadUtil = staticCompositionLocalOf<DownloadUtil> { error("No DownloadUtil provided") }