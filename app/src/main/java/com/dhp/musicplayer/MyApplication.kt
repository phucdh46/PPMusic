package com.dhp.musicplayer

import android.app.Application

class MyApplication: Application() {
    val TAG = "DHP"

    override fun onCreate() {
        super.onCreate()
        Preferences.initPrefs(applicationContext)

    }
}