package com.dhp.musicplayer.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.dhp.musicplayer.R
import com.dhp.musicplayer.ui.Icons

enum class TopLevelDestination(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val iconTextId: Int,
    val titleTextId: Int,
) {
    FOR_YOU(
    selectedIcon = Icons.Home,
    unselectedIcon = Icons.Home,
    iconTextId = R.string.feature_foryou_title,
    titleTextId = R.string.app_name,
    ),
    SEARCH(
    selectedIcon = Icons.Search,
    unselectedIcon = Icons.Search,
    iconTextId = R.string.feature_search_title,
    titleTextId = R.string.feature_search_title,
    ),
    LIBRARY(
    selectedIcon = Icons.LibraryMusic,
    unselectedIcon = Icons.LibraryMusic,
    iconTextId = R.string.feature_library_title,
    titleTextId = R.string.feature_library_title,
    ),
}