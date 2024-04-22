package com.dhp.musicplayer.utils

import android.content.Context
import android.content.Intent
import android.widget.Toast

inline fun <reified T> Context.intent(): Intent =
    Intent(this, T::class.java)

fun Context.toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
