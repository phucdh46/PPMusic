package com.dhp.musicplayer.core.common.extensions

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.widget.Toast

inline fun <reified T> Context.intent(): Intent = Intent(this, T::class.java)

fun Context.toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun Context.toast(messageId: Int) =
    Toast.makeText(this, this.getString(messageId), Toast.LENGTH_SHORT).show()

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}