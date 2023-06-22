package com.dhp.musicplayer

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

    }
}