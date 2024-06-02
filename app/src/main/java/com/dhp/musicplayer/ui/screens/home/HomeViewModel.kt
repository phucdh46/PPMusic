package com.dhp.musicplayer.ui.screens.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhp.musicplayer.constant.ConfigApiKey
import com.dhp.musicplayer.constant.RelatedMediaIdKey
import com.dhp.musicplayer.enums.UiState
import com.dhp.musicplayer.innertube.Innertube
import com.dhp.musicplayer.innertube.InnertubeApiService
import com.dhp.musicplayer.innertube.model.bodies.NextBody
import com.dhp.musicplayer.utils.dataStore
import com.dhp.musicplayer.utils.get
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
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Innertube.RelatedPage>>(UiState.Loading)
    val uiState: StateFlow<UiState<Innertube.RelatedPage>> = _uiState

    val isRefreshing = MutableStateFlow(false)

    init {
        fetchRelatedMedia()
    }

    private fun fetchRelatedMedia() {
        viewModelScope.launch {
            application.dataStore.data.distinctUntilChanged()
                .map { dataStore -> dataStore[ConfigApiKey] }
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
                                InnertubeApiService.getInstance(application)
                                    .relatedPage(NextBody(videoId = relatedMediaId))
                            }?.getOrNull()
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