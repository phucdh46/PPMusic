package com.dhp.musicplayer.feature.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhp.musicplayer.core.common.enums.UiState
import com.dhp.musicplayer.core.datastore.ApiConfigKey
import com.dhp.musicplayer.core.datastore.RelatedMediaIdKey
import com.dhp.musicplayer.core.datastore.dataStore
import com.dhp.musicplayer.core.datastore.get
import com.dhp.musicplayer.core.domain.repository.MusicRepository
import com.dhp.musicplayer.core.domain.repository.NetworkMusicRepository
import com.dhp.musicplayer.core.model.music.Playlist
import com.dhp.musicplayer.core.model.music.RelatedPage
import com.dhp.musicplayer.core.model.music.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val application: Application,
    private val networkMusicRepository: NetworkMusicRepository,
    private val musicRepository: MusicRepository,
) : ViewModel() {
    private val defaultSong = Song()
    val isRefreshing = MutableStateFlow(false)

    private val relatedMediaId =
        MutableStateFlow(application.dataStore.get(RelatedMediaIdKey, defaultSong.id))

    private val playlists = MutableStateFlow<List<Playlist>>(emptyList())

    init {
        fetchRelatedData()
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val uiState: Flow<UiState<RelatedPage?>> =
        relatedMediaId
            .flatMapLatest { relatedMediaId ->
                musicRepository.getRelatedSongs(relatedMediaId).debounce(500)
                    .map { relatedPage ->
                        if (relatedPage == null || (relatedPage.songs.isNullOrEmpty() && relatedPage.albums.isNullOrEmpty() && relatedPage.artists.isNullOrEmpty())) {
                            if (isRefreshing.value) UiState.Loading else
                                UiState.Error
                        } else {
                            isRefreshing.value = false
                            UiState.Success(relatedPage.copy(playlists = playlists.value))
                        }
                    }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = UiState.Loading
            )


    private fun fetchRelatedData() {
        viewModelScope.launch(Dispatchers.IO) {
            application.dataStore.data.distinctUntilChanged()
                .map { dataStore -> dataStore[ApiConfigKey] }.distinctUntilChanged()
                .collect { configApiKey ->
                    isRefreshing.value = true
                    if (configApiKey != null) {
                        try {
                            relatedMediaId.value = runBlocking {
                                application.dataStore[RelatedMediaIdKey].let {
                                    musicRepository.song(
                                        it
                                    ).first()?.id
                                } ?: defaultSong.id
                            }
                            if (relatedMediaId.value == defaultSong.id) {
                                musicRepository.insert(defaultSong)
                            }
                            val data = musicRepository.getRelatedSongs(relatedMediaId.value).first()
                            if (data == null || (data.songs.isNullOrEmpty() && data.albums.isNullOrEmpty() && data.artists.isNullOrEmpty())) {
                                val result = withContext(Dispatchers.IO) {
                                    networkMusicRepository.relatedPage(id = relatedMediaId.value)
                                }
                                try {
                                    if (result?.songs != null) {
//                            musicRepository.clearAllSongRelated()
                                        result.songs?.forEach { song ->
                                            musicRepository.insert(song)
                                            musicRepository.insertRelatedSong(
                                                relatedMediaId.value, song.id
                                            )
                                        }
                                    }
                                    if (result?.albums != null) {
//                            musicRepository.clearAllAlbumRelated()
                                        result.albums?.forEach { album ->
                                            musicRepository.insert(album)
                                            musicRepository.insertRelatedAlbum(
                                                relatedMediaId.value, album.id
                                            )
                                        }
                                    }

                                    if (result?.artists != null) {
//                            musicRepository.clearAllArtistRelated()
                                        result.artists?.forEach { artist ->
                                            musicRepository.insert(artist)
                                            musicRepository.insertRelatedArtist(
                                                relatedMediaId.value, artist.id
                                            )
                                        }
                                    }
                                    result?.playlists?.let {
                                        playlists.value = it
                                    }
                                    if (result == null) isRefreshing.value = false
                                } catch (e: Exception) {
                                    isRefreshing.value = false
                                }
                            } else {
                                isRefreshing.value = false
                            }
                        } catch (e: Exception) {
                            isRefreshing.value = false
                        }
                    }
                }
        }
    }

    fun refresh() {
        if (isRefreshing.value) return
        fetchRelatedData()
    }
}