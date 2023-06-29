package com.dhp.musicplayer

import android.support.v4.media.session.PlaybackStateCompat

class Constants {
    companion object {

        const val FAST_FORWARD_ACTION = "FAST_FORWARD"
        const val PREV_ACTION = "PREV"
        const val PLAY_PAUSE_ACTION = "PLAY_PAUSE"
        const val NEXT_ACTION = "NEXT"
        const val REWIND_ACTION = "REWIND"
        const val REPEAT_ACTION = "REPEAT"
        const val CLOSE_ACTION = "CLOSE"
        const val FAVORITE_ACTION = "FAVORITE"
        const val FAVORITE_POSITION_ACTION = "FAVORITE_POSITION"

        const val NOTIFICATION_CHANNEL_ID = "CHANNEL_PLAYBACK_SERVICE_MPGO"
        const val NOTIFICATION_CHANNEL_ERROR_ID = "CHANNEL_ERROR_MPGO"
        const val NOTIFICATION_INTENT_REQUEST_CODE = 100
        const val NOTIFICATION_ID = 101

        const val ARTIST_VIEW = "0"
        const val ALBUM_VIEW = "1"
        const val FOLDER_VIEW = "2"

        // Player playing statuses
        const val PLAYING = PlaybackStateCompat.STATE_PLAYING
        const val PAUSED = PlaybackStateCompat.STATE_PAUSED
        const val RESUMED = PlaybackStateCompat.STATE_NONE

        // sorting
        const val DEFAULT_SORTING = 0
        const val ASCENDING_SORTING = 1
        const val DESCENDING_SORTING = 2
        const val TRACK_SORTING = 3
        const val TRACK_SORTING_INVERTED = 4
        const val DATE_ADDED_SORTING = 5
        const val DATE_ADDED_SORTING_INV = 6
        const val ARTIST_SORTING = 7
        const val ARTIST_SORTING_INV = 8
        const val ALBUM_SORTING = 9
        const val ALBUM_SORTING_INV = 10

        // active fragments
        const val ARTISTS_TAB = "ARTISTS_TAB"
        const val ALBUM_TAB = "ALBUM_TAB"
        const val SONGS_TAB = "SONGS_TAB"
        const val FOLDERS_TAB = "FOLDERS_TAB"
        const val SETTINGS_TAB = "SETTINGS_TAB"

        val DEFAULT_ACTIVE_FRAGMENTS = listOf(ARTISTS_TAB, ALBUM_TAB, SONGS_TAB, FOLDERS_TAB, SETTINGS_TAB)

        const val RESTORE_FRAGMENT = "RESTORE_FRAGMENT"

    }
}