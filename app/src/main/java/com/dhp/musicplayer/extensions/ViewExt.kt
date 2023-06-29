package com.dhp.musicplayer.extensions

import android.app.Activity
import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.window.layout.WindowMetricsCalculator
import coil.load
import com.dhp.musicplayer.SingleClickHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior

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

fun View.safeClickListener(safeClickListener: (view: View) -> Unit) {
    setOnClickListener {
        if (!SingleClickHelper.isBlockingClick()) safeClickListener(it)
    }
}

fun Dialog?.applyFullHeightDialog(activity: Activity) {
    // to ensure full dialog's height

    val windowMetrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(activity)
    val height = windowMetrics.bounds.height()

    this?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)?.let { bs ->
        BottomSheetBehavior.from(bs).peekHeight = height
    }
}

// Extension to set menu items text color
fun MenuItem.setTitleColor(color: Int) {
    SpannableString(title).apply {
        setSpan(ForegroundColorSpan(color), 0, length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        title = this
    }
}

fun View.handleViewVisibility(show: Boolean) {
    visibility = if (show) View.VISIBLE else View.GONE
}

fun ImageView.updateIconTint(tint: Int) {
    ImageViewCompat.setImageTintList(this, ColorStateList.valueOf(tint))
}

// Extension to set menu items icon color
fun MenuItem.setIconTint(color: Int) {
    icon?.let { dw ->
        val wrapped = DrawableCompat.wrap(dw)
        DrawableCompat.setTint(wrapped, color)
        icon = wrapped
    }
}

fun ImageView.loadWithError(bitmap: Bitmap?, error: Boolean, albumArt: Int) {
    if (error) {
        scaleType = ImageView.ScaleType.CENTER_INSIDE
        load(ContextCompat.getDrawable(context, albumArt)?.toBitmap())
        return
    }
    scaleType = ImageView.ScaleType.CENTER_CROP
    load(bitmap)
}