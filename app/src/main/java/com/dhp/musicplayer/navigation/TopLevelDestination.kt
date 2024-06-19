package com.dhp.musicplayer.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.dhp.musicplayer.R
import com.dhp.musicplayer.core.designsystem.icon.IconApp

enum class TopLevelDestination(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val iconTextId: Int,
    val titleTextId: Int,
) {
    FOR_YOU(
    selectedIcon = IconApp.Home,
    unselectedIcon = IconApp.Home,
    iconTextId = R.string.feature_home_title,
    titleTextId = R.string.app_name,
    ),
    SEARCH(
    selectedIcon = IconApp.Search,
    unselectedIcon = IconApp.Search,
    iconTextId = R.string.feature_search_title,
    titleTextId = R.string.feature_search_title,
    ),
    LIBRARY(
    selectedIcon = IconApp.LibraryMusic,
    unselectedIcon = IconApp.LibraryMusic,
    iconTextId = R.string.feature_library_title,
    titleTextId = R.string.feature_library_title,
    ),
}