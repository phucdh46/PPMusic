package com.dhp.musicplayer

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.dhp.musicplayer.core.model.music.RadioEndpoint
import com.dhp.musicplayer.core.model.music.Song
import com.dhp.musicplayer.utils.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class AppFireBaseMessagingService : FirebaseMessagingService() {

    @OptIn(UnstableApi::class)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val titleNotification = remoteMessage.data[TITLE_NOTIFICATION]
        val bodyNotification = remoteMessage.data[BODY_NOTIFICATION]
        val id = remoteMessage.data[ID]
        val title = remoteMessage.data[TITLE]
        val artistsText = remoteMessage.data[ARTIST_TEXT]
        val thumbnailUrl = remoteMessage.data[THUMBNAIL_URL]
        id?.let {
            val song = Song().copy(
                id = it,
                title = title.orEmpty(),
                artistsText = artistsText,
                thumbnailUrl = thumbnailUrl,
                radioEndpoint = RadioEndpoint(videoId = it)
            )
            NotificationHelper(this@AppFireBaseMessagingService).showSongNotification(
                titleNotification,
                bodyNotification,
                song
            )
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    companion object {
        const val TITLE_NOTIFICATION = "titleNotification"
        const val BODY_NOTIFICATION = "bodyNotification"
        const val ID = "id"
        const val TITLE = "title"
        const val ARTIST_TEXT = "artistsText"
        const val THUMBNAIL_URL = "thumbnailUrl"
    }
}