package com.dhp.musicplayer.feature.library.songs.downloaded

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import com.dhp.musicplayer.core.common.enums.UiState
import com.dhp.musicplayer.data.repository.MusicRepository
import com.dhp.musicplayer.core.services.download.DownloadUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@UnstableApi
@HiltViewModel
class DownloadSongsViewModel
@Inject constructor(
    musicRepository: MusicRepository,
    downloadUtil: DownloadUtil,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState = downloadUtil.downloads.flatMapLatest { downloads ->
        musicRepository.getAllSongs().map { songs ->
            songs.filter {
                downloads[it.id]?.state == Download.STATE_COMPLETED
            }
        }.map {
            if (it.isEmpty()) {
                UiState.Empty
            } else {
                UiState.Success(it)
            }
        }

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UiState.Loading
    )
}