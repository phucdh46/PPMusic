package com.dhp.musicplayer.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.dhp.musicplayer.core.designsystem.R
import com.dhp.musicplayer.core.designsystem.icon.IconApp

enum class TopLevelDestination(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val iconLabelTextId: Int,
    val titleTextId: Int,
) {
    FOR_YOU(
    selectedIcon = IconApp.Home,
    unselectedIcon = IconApp.Home,
    iconLabelTextId = R.string.home_screen_title,
    titleTextId = R.string.app_name,
    ),
    SEARCH(
    selectedIcon = IconApp.Search,
    unselectedIcon = IconApp.Search,
    iconLabelTextId = R.string.search_screen_title,
    titleTextId = R.string.search_screen_title,
    ),
    LIBRARY(
    selectedIcon = IconApp.LibraryMusic,
    unselectedIcon = IconApp.LibraryMusic,
    iconLabelTextId = R.string.library_screen_title,
    titleTextId = R.string.library_screen_title,
    ),
}