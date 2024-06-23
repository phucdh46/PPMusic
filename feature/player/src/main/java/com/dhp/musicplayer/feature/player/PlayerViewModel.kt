package com.dhp.musicplayer.feature.player

import android.content.Context
import androidx.lifecycle.ViewModel
import com.dhp.musicplayer.core.domain.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val musicRepository: MusicRepository,
) : ViewModel() {

    fun isFavoriteSong(songId: String?) = musicRepository.isFavoriteSong(songId)

}