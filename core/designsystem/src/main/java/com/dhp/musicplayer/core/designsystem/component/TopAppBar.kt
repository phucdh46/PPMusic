package com.dhp.musicplayer.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dhp.musicplayer.core.designsystem.constant.TopBarHeight
import com.dhp.musicplayer.core.designsystem.R
import com.dhp.musicplayer.core.designsystem.icon.IconApp
import com.dhp.musicplayer.core.designsystem.theme.bold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    modifier: Modifier = Modifier,
    visible: Boolean,
    title: String,
    showBackButton: Boolean = true,
    showSearchButton: Boolean = false,
    showSettingButton: Boolean = true,
    colors: TopAppBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors(),
    onBackClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onSettingClick: () -> Unit = {},
) {
    AnimatedVisibility(
        visible = visible,
        enter = EnterTransition.None,
        exit = ExitTransition.None
    ) {
        Row(
            modifier = modifier
                .padding(
                    top = WindowInsets.systemBars
                        .asPaddingValues()
                        .calculateTopPadding()
                )
                .padding(horizontal = 8.dp)
                .height(TopBarHeight)
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically

        ) {
            AnimatedVisibility(visible = showBackButton) {
                IconButton(onClick = { onBackClick() }) {
                    Icon(imageVector = IconApp.ArrowBackIosNew, contentDescription = null)
                }
            }
            AnimatedVisibility(visible = !showBackButton) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.titleMedium.bold(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            AnimatedVisibility(visible = showSearchButton) {
                IconButton(onClick = { onSearchClick() }) {
                    Icon(imageVector = IconApp.Search, contentDescription = null)
                }
            }
            AnimatedVisibility(visible = showSettingButton) {
                IconButton(onClick = { onSettingClick() }) {
                    Icon(imageVector = IconApp.Settings, contentDescription = null)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarDetailScreen(
    title: @Composable () -> Unit = {
        Text(
            text = "",
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(horizontal = 8.dp)
        )
    },
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    colors: TopAppBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors(),
    onBackClick: () -> Unit = {},
    onMenuClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
//            .padding(
//                top = WindowInsets.systemBars
//                    .asPaddingValues()
//                    .calculateTopPadding()
//            )
            .height(TopBarHeight +  WindowInsets.systemBars
                .asPaddingValues()
                .calculateTopPadding())
            .background(backgroundColor)
            .padding(horizontal = 8.dp)
            .fillMaxSize()
                            .padding(
                top = WindowInsets.systemBars
                    .asPaddingValues()
                    .calculateTopPadding()
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        IconButton(onClick = { onBackClick() }) {
            Icon(imageVector = IconApp.ArrowBackIosNew, contentDescription = null)
        }

        title()
        Spacer(modifier = Modifier.weight(1f))
        onMenuClick?.let {
            IconButton(onClick = { onMenuClick() }) {
                Icon(imageVector = IconApp.MoreVert, contentDescription = null)
            }
        }
    }
}
