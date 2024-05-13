package com.dhp.musicplayer.ui.component

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import com.dhp.musicplayer.ui.IconApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    titleRes: String,
    showBackButton: Boolean = true,
    showSearchButton: Boolean = false,
    actionIcon: ImageVector,
    actionIconContentDescription: String,
    modifier: Modifier = Modifier,
    colors: TopAppBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors(),
    onNavigationClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onActionClick: () -> Unit = {},
) {
    CenterAlignedTopAppBar(
        title = { Text(titleRes) },
        navigationIcon = {
            when {
                showBackButton -> {
                    IconButton(onClick = onNavigationClick) {
                        Icon(
                            imageVector = IconApp.ArrowBackIosNew,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                showSearchButton -> {
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = IconApp.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

        },
        actions = {
            IconButton(onClick = onActionClick) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = actionIconContentDescription,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        colors = colors,
        modifier = modifier.testTag("niaTopAppBar"),
    )
}

//@OptIn(ExperimentalMaterial3Api::class)
//@Preview("Top App Bar")
//@Composable
//private fun NiaTopAppBarPreview() {
//    ComposeTheme {
//        TopAppBar(
//            titleRes = stringResource(id = R.string.untitled),
//            navigationIcon = Icons.Search,
////            navigationIconContentDescription = "Navigation icon",
//            actionIcon = Icons.MoreVert,
//            actionIconContentDescription = "Action icon",
//        )
//    }
//}