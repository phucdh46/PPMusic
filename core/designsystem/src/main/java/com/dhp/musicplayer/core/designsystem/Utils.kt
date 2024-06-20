package com.dhp.musicplayer.core.designsystem

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.google.material.color.score.Score

fun Bitmap.extractThemeColor(): Color {
    val colorsToPopulation = Palette.from(this)
        .maximumColorCount(8)
        .generate()
        .swatches
        .associate { it.rgb to it.population }
    val rankedColors = Score.score(colorsToPopulation)
    return Color(rankedColors.first())
}

suspend fun getArtworkColor(context: Context, url: String?): Color {
    try {
        url ?: return Color.Transparent
        val request = ImageRequest.Builder(context).data(url).allowHardware(false).build()
        val result = (ImageLoader(context).execute(request) as? SuccessResult)?.drawable
            ?: return Color.Transparent
        val palette = Palette.from((result as BitmapDrawable).bitmap).generate()
        val list = listOfNotNull(
            palette.mutedSwatch,
            palette.lightMutedSwatch,
            palette.vibrantSwatch,
            palette.lightVibrantSwatch,
            palette.darkVibrantSwatch,
            palette.darkMutedSwatch,
        )
        return Color(list.first().rgb)
    } catch (e: Throwable) {
        return Color.Transparent
    }
}
