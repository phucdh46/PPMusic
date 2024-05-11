package com.dhp.musicplayer.extensions

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.dhp.musicplayer.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

inline fun <reified T : Any> Fragment.extraNotNull(key: String, default: T? = null) = lazy {
    val value = arguments?.get(key)
    requireNotNull(if (value is T) value else default) { key }
}

inline fun <reified T : Any> Fragment.extra(key: String, default: T? = null) = lazy {
    val value = arguments?.get(key)
    if (value is T) value else default
}
