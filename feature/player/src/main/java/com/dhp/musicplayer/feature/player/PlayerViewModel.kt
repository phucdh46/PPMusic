package com.dhp.musicplayer.feature.player

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import com.dhp.musicplayer.core.domain.repository.MusicRepository
import com.dhp.musicplayer.core.services.extensions.toSong
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val musicRepository: MusicRepository,
) : ViewModel() {

    fun likeAt(songId: String?) = musicRepository.likedAt(songId)

    fun favourite(mediaItem: MediaItem) {
        viewModelScope.launch(Dispatchers.IO) {
            val likedAt = musicRepository.isFavorite(mediaItem.mediaId) != null
            if (musicRepository.favorite(
                    mediaItem.mediaId,
                    if (!likedAt) System.currentTimeMillis() else null
                ) == 0
            ) {
                musicRepository.insert(mediaItem.toSong().toggleLike())
            }
        }
    }
}