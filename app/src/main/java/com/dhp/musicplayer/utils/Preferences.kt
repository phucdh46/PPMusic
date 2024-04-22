package com.dhp.musicplayer.utils

import android.content.Context
import android.content.SharedPreferences
import com.dhp.musicplayer.enums.RepeatMode

const val queueLoopEnabledKey = "queueLoopEnabled"
const val trackLoopEnabledKey = "trackLoopEnabled"

val Context.preferences: SharedPreferences
    get() = getSharedPreferences("preferences", Context.MODE_PRIVATE)

// Extension function to write an Int value to SharedPreferences
fun SharedPreferences.putInt(key: String, value: Int) {
    edit().putInt(key, value).apply()
}

fun SharedPreferences.getInt(key: String, defaultValue: Int): Int {
    return getInt(key, defaultValue)
}

// Extension function to write a RepeatMode enum value to SharedPreferences
fun SharedPreferences.putRepeatMode(key: String, repeatMode: RepeatMode) {
    edit().putInt(key, repeatMode.value).apply()
}

// Extension function to read a RepeatMode enum value from SharedPreferences
fun SharedPreferences.getRepeatMode(key: String, repeatMode: RepeatMode = RepeatMode.NONE): RepeatMode {
    val storedValue = getInt(key, repeatMode.value)
    return RepeatMode.fromInt(storedValue)
}

inline fun <reified T : Enum<T>> SharedPreferences.Editor.putEnum(
    key: String,
    value: T
): SharedPreferences.Editor =
    putString(key, value.name)