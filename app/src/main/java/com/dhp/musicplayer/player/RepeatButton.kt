package com.dhp.musicplayer.player

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.dhp.musicplayer.R
import com.dhp.musicplayer.enums.RepeatMode

interface CustomButtonClickListener {
    fun onStateChange(newState: RepeatMode)
}

class RepeatButton(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var currentState = RepeatMode.NONE // Initial state

    private var iconNone: Drawable? = null
    private var iconOne: Drawable? = null
    private var iconAll: Drawable? = null

    private val iconPaint = Paint()

    private var clickListener: CustomButtonClickListener? = null // Listener for state changes


    init {
        // Load icons from resources
        iconNone = ContextCompat.getDrawable(context, R.drawable.repeat_24)
        iconOne = ContextCompat.getDrawable(context, R.drawable.repeat_one_24)
        iconAll = ContextCompat.getDrawable(context, R.drawable.repeat_24_enable)

        // Set initial click listener to change state on click
        setOnClickListener {
            currentState = when (currentState) {
                RepeatMode.NONE -> RepeatMode.ONE
                RepeatMode.ONE -> RepeatMode.ALL
                RepeatMode.ALL -> RepeatMode.NONE
            }
            invalidate() // Redraw the CustomButton when state changes
            clickListener?.onStateChange(currentState)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Determine the icon based on the current state
        val currentIcon = when (currentState) {
            RepeatMode.NONE -> iconNone
            RepeatMode.ONE -> iconOne
            RepeatMode.ALL -> iconAll
            else -> iconNone // Default to state 1
        }

        // Draw the icon at the center of the CustomButton
        currentIcon?.let {
            val centerX = width / 2
            val centerY = height / 2
            val iconWidth = it.intrinsicWidth
            val iconHeight = it.intrinsicHeight
            val iconLeft = centerX - iconWidth / 2
            val iconTop = centerY - iconHeight / 2
            val iconRight = iconLeft + iconWidth
            val iconBottom = iconTop + iconHeight
            it.bounds = Rect(iconLeft, iconTop, iconRight, iconBottom)
            it.draw(canvas!!)
        }
    }

    // Public method to retrieve the current state of the button
    fun getCurrentState(): RepeatMode {
        return currentState
    }

    // Method to set the listener for state changes
    fun setCustomButtonClickListener(listener: CustomButtonClickListener) {
        this.clickListener = listener
    }

    // Method to set the state of the button to a specific repeat mode
    fun setState(newRepeatMode: RepeatMode) {
        currentState = newRepeatMode
        invalidate() // Redraw the CustomButton with the new state
    }
}