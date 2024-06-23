package com.dhp.musicplayer.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.dhp.musicplayer.core.designsystem.R
import com.dhp.musicplayer.core.designsystem.extensions.shimmer

@Composable
fun LoadingShimmerImage(
    modifier: Modifier = Modifier,
    thumbnailUrl: String? = null,
    contentDescription: String? = null,
    thumbnailSizeDp: Dp,
    visibility: Float = 0f
) {
    SubcomposeAsyncImage(
        model = thumbnailUrl,
        contentDescription = contentDescription,
        modifier = modifier
            .graphicsLayer {
                alpha = 1f - visibility
//                                translationY = firstItemTranslationY
            }
    ) {
        val state = painter.state
        when (state) {
            is AsyncImagePainter.State.Loading -> {
                Box(
                    modifier = modifier
                        .size(thumbnailSizeDp)
                        .shimmer()
                )
            }

            is AsyncImagePainter.State.Error -> {
                Image(
                    painter = painterResource(id = R.drawable.logo_grayscale),
                    contentDescription = null,
                    modifier = modifier.size(thumbnailSizeDp)
                )
            }

            else -> {
                SubcomposeAsyncImageContent()
            }
        }
    }
}

@Composable
fun LoadingShimmerImageMaxSize(
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    thumbnailUrl: String? = null,
    contentDescription: String? = null,
) {
    SubcomposeAsyncImage(
        model = thumbnailUrl,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier
    ) {
        val state = painter.state
        when (state) {
            is AsyncImagePainter.State.Loading -> {
                Box(
                    modifier = modifier
                        .fillMaxSize()
                        .shimmer()
                )
            }

            is AsyncImagePainter.State.Error -> {
                Image(
                    painter = painterResource(id = R.drawable.logo_grayscale),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }

            else -> {
                SubcomposeAsyncImageContent()
            }
        }
    }
}

@Composable
fun ImageNotLoading(
    modifier: Modifier = Modifier,
    url: String?
) {
    AsyncImage(
        modifier = modifier,
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(true)
            .build(),
        error = painterResource(id = R.drawable.logo_grayscale),
        contentDescription = null,
        contentScale = ContentScale.Crop,
    )
}