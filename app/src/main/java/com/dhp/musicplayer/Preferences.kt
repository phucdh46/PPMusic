package com.dhp.musicplayer

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.dhp.musicplayer.model.Music
import com.squareup.moshi.Moshi

class Preferences(context: Context) {
    private val mPrefs = PreferenceManager.getDefaultSharedPreferences(context)
    private val mMoshi = Moshi.Builder().build()

    var latestPlayedSong: Music?
        get() = getObjectForClass("to_restore_song_pref", Music::class.java)
        set(value) = putObjectForClass("to_restore_song_pref", value, Music::class.java)

    private fun <T : Any> getObjectForClass(key: String, clazz: Class<T>): T? {
        val json = mPrefs.getString(key, null)
        return if (json == null) {
            null
        } else {
            try {
                mMoshi.adapter(clazz).fromJson(json)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    // Saves object into the Preferences using Moshi
    private fun <T : Any> putObjectForClass(key: String, value: T?, clazz: Class<T>) {
        val json = mMoshi.adapter(clazz).toJson(value)
        mPrefs.edit { putString(key, json) }
    }

    companion object {

        // Singleton prevents multiple instances of GoPreferences opening at the
        // same time.
        @Volatile
        private var INSTANCE: Preferences? = null

        fun initPrefs(context: Context): Preferences {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the preferences
            return INSTANCE ?: synchronized(this) {
                val instance = Preferences(context)
                INSTANCE = instance
                // return instance
                instance
            }
        }

        fun getPrefsInstance(): Preferences {
            return INSTANCE ?: error("Preferences not initialized!")
        }
    }

}