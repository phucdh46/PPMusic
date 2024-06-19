package com.dhp.musicplayer.core.common.extensions

import android.content.Context
import android.content.Intent

inline fun <reified T> Context.intent(): Intent = Intent(this, T::class.java)
