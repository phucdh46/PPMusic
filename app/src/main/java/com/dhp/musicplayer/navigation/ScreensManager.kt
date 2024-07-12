package com.dhp.musicplayer.navigation

import com.dhp.musicplayer.feature.home.navigation.FOR_YOU_ROUTE
import com.dhp.musicplayer.feature.library.navigation.LIBRARY_ROUTE
import com.dhp.musicplayer.feature.library.songs.detail.navigation.LIBRARY_SONGS_DETAIL_ROUTE
import com.dhp.musicplayer.feature.playlist.local.navigation.LOCAL_PLAYLIST_DETAIL_ROUTE
import com.dhp.musicplayer.feature.playlist.online.navigation.ONLINE_PLAYLIST_DETAIL_ROUTE
import com.dhp.musicplayer.feature.search.main.navigation.EXPLORE_ROUTE
import com.dhp.musicplayer.feature.search.mood_genres_detail.navigation.MOOD_AND_GENRES_ROUTE
import com.dhp.musicplayer.feature.search.search_by_text.navigation.SEARCH_BY_TEXT_ROUTE
import com.dhp.musicplayer.feature.search.search_result.navigation.SEARCH_RESULT_ROUTE
import com.dhp.musicplayer.feature.settings.SETTINGS_ROUTE

val ScreensShowBottomNavigation = listOf(FOR_YOU_ROUTE, EXPLORE_ROUTE, LIBRARY_ROUTE)
val ScreensShowBackOnTopAppBar = listOf(SEARCH_BY_TEXT_ROUTE, LOCAL_PLAYLIST_DETAIL_ROUTE)
val ScreensShowSearchOnTopAppBar = listOf(FOR_YOU_ROUTE, EXPLORE_ROUTE, LIBRARY_ROUTE)
val ScreensNotShowTopAppBar = listOf(SEARCH_BY_TEXT_ROUTE, LOCAL_PLAYLIST_DETAIL_ROUTE, ONLINE_PLAYLIST_DETAIL_ROUTE, SEARCH_RESULT_ROUTE, MOOD_AND_GENRES_ROUTE, LIBRARY_SONGS_DETAIL_ROUTE)
val ScreensNotShowSettingButton = listOf(SETTINGS_ROUTE)

