package com.dhp.musicplayer.core.datastore

import android.content.Context
import com.dhp.musicplayer.core.model.settings.ApiKey
import kotlinx.serialization.json.Json

fun Context.getConfig(): ApiKey {
    return try {
        val key = dataStore[ApiConfigKey] ?: return ApiKey()
        Json.decodeFromString(ApiKey.serializer(), key)
    } catch (e: Exception) {
        ApiKey()
    }
}