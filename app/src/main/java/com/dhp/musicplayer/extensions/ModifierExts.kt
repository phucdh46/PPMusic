package com.dhp.musicplayer.extensions

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

fun Modifier.shimmer(
    durationMillis: Int = 2500
) = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
            )
        ), label = "shimmer"
    )
    drawShimmer(progress)
}

fun Modifier.drawShimmer(progress: Float) = this.then(
    Modifier
        .drawWithContent {
            val width = size.width
            val height = size.height
            val offset = progress * width

            drawContent()
            val brush = Brush.linearGradient(
                colors = listOf(
                    Color.LightGray,
                    Color.LightGray,
                    Color.White,
                    Color.LightGray,
                    Color.LightGray
                ),
                start = Offset(offset, 0f),
                end = Offset(offset + width, height)
            )
            drawRect(brush)
        }
)

fun Modifier.drawOneSideBorder(
    width: Dp,
    color: Color,
    shape: Shape = RectangleShape
) = this
    .clip(shape)
    .drawWithContent {
        val widthPx = width.toPx()
        drawContent()
        drawLine(
            color = color,
            start = Offset(widthPx / 2, 0f),
            end = Offset(widthPx / 2, size.height),
            strokeWidth = widthPx
        )
    }