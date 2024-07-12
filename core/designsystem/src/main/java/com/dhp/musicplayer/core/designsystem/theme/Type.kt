package com.dhp.musicplayer.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// Align
fun TextStyle.start() = this.merge(TextStyle(textAlign = TextAlign.Start))
fun TextStyle.center() = this.merge(TextStyle(textAlign = TextAlign.Center))
fun TextStyle.end() = this.merge(TextStyle(textAlign = TextAlign.End))

// Style
fun TextStyle.bold() = this.merge(TextStyle(fontWeight = FontWeight.Bold))
fun TextStyle.extraBold() = this.merge(TextStyle(fontWeight = FontWeight.ExtraBold))
fun TextStyle.italic() = this.merge(TextStyle(fontStyle = FontStyle.Italic))

// Padding
fun TextStyle.excludeFontPadding() = this.merge(TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)))
