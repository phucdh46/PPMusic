package com.dhp.musicplayer.constant

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

val PersistentQueueDataKey = stringPreferencesKey("persistentQueueData")
val RepeatModeKey = intPreferencesKey("repeatMode")
val PlaylistViewTypeKey = stringPreferencesKey("playlistViewType")
