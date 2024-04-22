package com.dhp.musicplayer.extensions

import android.content.res.ColorStateList
import androidx.annotation.ColorInt
import com.google.android.material.slider.Slider

fun Slider.applyColor(@ColorInt color: Int) {
    ColorStateList.valueOf(color).run {
        thumbTintList = this
        trackActiveTintList = this
//        trackInactiveTintList = ColorStateList.valueOf(color.addAlpha(0.1f))
        haloTintList = this
    }
}