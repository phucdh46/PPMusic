package com.dhp.musicplayer.extensions

import android.util.Log
import android.view.View
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

fun View.applyEdgeToEdge() {
    setOnApplyWindowInsetsListener { view, insets ->
        val bars = WindowInsetsCompat.toWindowInsetsCompat(insets)
            .getInsets(WindowInsetsCompat.Type.systemBars())
        view.updatePadding(left = bars.left, right = bars.right)
        insets
    }
}

fun ViewPager2.reduceDragSensitivity() {

    // By default, ViewPager2's sensitivity is high enough to result in vertical
    // scroll events being registered as horizontal scroll events. Reflect into the
    // internal recyclerview and change the touch slope so that touch actions will
    // act more as a scroll than as a swipe.
    try {

        val recycler = ViewPager2::class.java.getDeclaredField("mRecyclerView")
        recycler.isAccessible = true
        val recyclerView = recycler.get(this) as RecyclerView

        val touchSlopField = RecyclerView::class.java.getDeclaredField("mTouchSlop")
        touchSlopField.isAccessible = true
        val touchSlop = touchSlopField.get(recyclerView) as Int
        touchSlopField.set(recyclerView, touchSlop*3) // 3x seems to be the best fit here

    } catch (e: Exception) {
        Log.e("MainActivity", "Unable to reduce ViewPager sensitivity")
        Log.e("MainActivity", e.stackTraceToString())
    }
}