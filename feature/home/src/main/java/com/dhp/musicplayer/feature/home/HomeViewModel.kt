package com.dhp.musicplayer.feature.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhp.musicplayer.core.common.enums.UiState
import com.dhp.musicplayer.core.domain.repository.NetworkMusicRepository
import com.dhp.musicplayer.core.model.music.RelatedPage
import com.dhp.musicplayer.core.datastore.ApiConfigKey
import com.dhp.musicplayer.core.datastore.RelatedMediaIdKey
import com.dhp.musicplayer.core.datastore.dataStore
import com.dhp.musicplayer.core.datastore.get
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val application: Application,
    private val networkMusicRepository: NetworkMusicRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<RelatedPage>>(UiState.Loading)
    val uiState: StateFlow<UiState<RelatedPage>> = _uiState

    val isRefreshing = MutableStateFlow(false)

    init {
        fetchRelatedMedia()
    }

    private fun fetchRelatedMedia() {
        viewModelScope.launch {
            application.dataStore.data.distinctUntilChanged()
                .map { dataStore -> dataStore[ApiConfigKey] }
                .distinctUntilChanged()
                .collect { configApiKey ->
                    if (configApiKey != null) {
                        isRefreshing.value = true
                        _uiState.value = UiState.Loading
                        try {
                            val relatedMediaId = withContext(Dispatchers.IO) {
                                application.dataStore[RelatedMediaIdKey] ?: "xl8thVrlvjI"
                            }
                            val result = withContext(Dispatchers.IO) {
                                networkMusicRepository.relatedPage(id = relatedMediaId)
                            }
                            if (result != null) {
                                _uiState.value = UiState.Success(result)
                            } else {
                                _uiState.value = UiState.Error
                            }
                        } catch (e: Exception) {
                            _uiState.value = UiState.Error
                        } finally {
                            isRefreshing.value = false
                        }
                    } else {
                        _uiState.value = UiState.Error
                    }
                }
        }
    }

    fun refresh() {
        if (isRefreshing.value) return
        fetchRelatedMedia()
    }
}