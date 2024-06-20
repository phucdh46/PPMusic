package com.dhp.musicplayer.core.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

val PersistentQueueDataKey = stringPreferencesKey("persistentQueueData")
val RepeatModeKey = intPreferencesKey("repeatMode")
val PlaylistViewTypeKey = stringPreferencesKey("playlistViewType")
val RelatedMediaIdKey = stringPreferencesKey("relatedMediaId")
val LastTimeUserCancelFlexibleUpdateKey = longPreferencesKey("lastTimeUserCancelFlexibleUpdate")
val ApiConfigKey = stringPreferencesKey("apiConfigKey")
val DarkThemeConfigKey = stringPreferencesKey("darkThemeConfig")
val DynamicThemeKey = booleanPreferencesKey("dynamicTheme")
