package com.dhp.musicplayer.core.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.dhp.musicplayer.core.designsystem.R

@Composable
fun EmptyList(
    text: String, floatContent: (@Composable BoxScope.() -> Unit)? = null,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_grayscale),
                contentDescription = null,
//                colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply {
//                    setToSaturation(0f)
//                })
            )
            Spacer(modifier = Modifier.padding(16.dp))
            Text(text = text, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        floatContent?.invoke(this)
    }

}