package com.dhp.musicplayer.constant

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

val PersistentQueueDataKey = stringPreferencesKey("persistentQueueData")
val RepeatModeKey = intPreferencesKey("repeatMode")
val PlaylistViewTypeKey = stringPreferencesKey("playlistViewType")
val RelatedMediaIdKey = stringPreferencesKey("relatedMediaId")
val LastTimeUserCancelFlexibleUpdateKey = longPreferencesKey("lastTimeUserCancelFlexibleUpdate")
val ConfigApiKey = stringPreferencesKey("configApi")
val DarkThemeConfigKey = stringPreferencesKey("darkThemeConfig")
