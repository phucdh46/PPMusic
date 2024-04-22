package com.dhp.musicplayer

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.dhp.musicplayer.utils.Theming
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication: Application() {
    val TAG = "DHP"

    override fun onCreate() {
        super.onCreate()
        Preferences.initPrefs(applicationContext)
        AppCompatDelegate.setDefaultNightMode(Theming.getDefaultNightMode(applicationContext))

    }
}