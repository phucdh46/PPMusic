package com.dhp.musicplayer.extensions

import android.view.View
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

fun View.applyEdgeToEdge() {
    setOnApplyWindowInsetsListener { view, insets ->
        val bars = WindowInsetsCompat.toWindowInsetsCompat(insets)
            .getInsets(WindowInsetsCompat.Type.systemBars())
        view.updatePadding(left = bars.left, right = bars.right)
        insets
    }
}