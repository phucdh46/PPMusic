package com.dhp.musicplayer

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.dhp.musicplayer.model.Music
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.Type

class Preferences(context: Context) {
    private val mPrefs = PreferenceManager.getDefaultSharedPreferences(context)
    private val mMoshi = Moshi.Builder().build()
    // active fragments type
    private val typeActiveTabs = Types.newParameterizedType(List::class.java, String::class.java)

    var latestPlayedSong: Music?
        get() = getObjectForClass("to_restore_song_pref", Music::class.java)
        set(value) = putObjectForClass("to_restore_song_pref", value, Music::class.java)

    var allMusicSorting
        get() = mPrefs.getInt("sorting_all_music_tab_pref", Constants.DEFAULT_SORTING)
        set(value) = mPrefs.edit { putInt("sorting_all_music_tab_pref", value) }

    var accent
        get() = mPrefs.getInt("color_primary_pref", 3)
        set(value) = mPrefs.edit { putInt("color_primary_pref", value) }

    var activeTabs: List<String>
        get() = getObjectForType("active_tabs_pref", typeActiveTabs)
            ?: Constants.DEFAULT_ACTIVE_FRAGMENTS
        set(value) = putObjectForType("active_tabs_pref", value, typeActiveTabs)

    var activeTabsDef: List<String>
        get() = getObjectForType("active_tabs_def_pref", typeActiveTabs)
            ?: Constants.DEFAULT_ACTIVE_FRAGMENTS
        set(value) = putObjectForType("active_tabs_def_pref", value, typeActiveTabs)

    var theme
        get() = mPrefs.getString("theme_pref", "theme_pref_auto")
        set(value) = mPrefs.edit { putString("theme_pref", value) }

    var isBlackTheme: Boolean
        get() = mPrefs.getBoolean("theme_pref_black", false)
        set(value) = mPrefs.edit { putBoolean("theme_pref_black", value) }

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

    // Retrieve object from the Preferences using Moshi
    private fun <T : Any> putObjectForType(key: String, value: T?, type: Type) {
        val json = mMoshi.adapter<T>(type).toJson(value)
        mPrefs.edit { putString(key, json) }
    }

    private fun <T : Any> getObjectForType(key: String, type: Type): T? {
        val json = mPrefs.getString(key, null)
        return if (json == null) {
            null
        } else {
            try {
                mMoshi.adapter<T>(type).fromJson(json)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
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