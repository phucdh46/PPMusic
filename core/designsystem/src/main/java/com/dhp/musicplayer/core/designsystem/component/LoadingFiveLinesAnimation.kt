package com.dhp.musicplayer.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dhp.musicplayer.core.designsystem.icon.IconApp

@Preview
@Composable
fun LoadingFiveLinesCenterPreview() {
    LoadingFiveLinesCenter()
}

@Preview
@Composable
fun LoadingFiveLinesCenterPreviewPlay() {
    LoadingFiveLinesCenter(isPlaying = true)
}

@Composable
fun LoadingFiveLinesCenter(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    size: Int = 50,
    speed: Double = 0.5,
    isPlaying: Boolean = false,
    isShow: Boolean = true
) {
    AnimatedVisibility(
        visible = isShow,
        modifier = modifier
//            .background(Color.Black.copy(alpha = 0.4f))
            .fillMaxSize()
    ) {
        val maxCounter = if (isPlaying) 4 else 2

        if (isPlaying) {
            Row(
                Modifier.height(size.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.width(size.div(10).dp))
                repeat(maxCounter) { index ->
                    EachRect(size * 3 / 4, index, color, speed, isPlaying)
                    Spacer(modifier = Modifier.width(size.div(10).dp))
                }
            }
        } else {
            Row(
                Modifier.height(size.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(imageVector = IconApp.PlayArrow, contentDescription = "play", tint = color)
            }
        }
    }
}

@Composable
private fun EachRect(
    size: Int,
    index: Int,
    color: Color,
    speed: Double,
    play: Boolean
) {
    val timing = speed.times(1000)
    val delay =
        (if (index == 2) 0.0 else if (index == 1 || index == 3) (timing / 3) else (timing / 3) * 2).toInt()
    val animateInfinite = rememberInfiniteTransition(label = "InfiniteTransition")
    val height by animateInfinite.animateFloat(
        initialValue = (size / 3).toFloat(),
        targetValue = size.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = timing.toInt(),
                delayMillis = delay,
                easing = LinearEasing
            ), repeatMode = RepeatMode.Reverse
        ),
        label = "FloatAnimation"
    )
    val heightState = if (play) height else (size / 3).toFloat()

    Box(
        modifier = Modifier
            .height(heightState.dp)
            .width(6.dp)
            .background(color, shape = RoundedCornerShape(50))
    )
}