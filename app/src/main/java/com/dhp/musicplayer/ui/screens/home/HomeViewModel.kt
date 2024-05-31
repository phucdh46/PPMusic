package com.dhp.musicplayer.ui.screens.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
            isRefreshing.value = true
            _uiState.value = UiState.Loading
            try {
                val relatedMediaId = withContext(Dispatchers.IO) {
                    application.dataStore[RelatedMediaIdKey] ?: "J7p4bzqLvCw"
                }
                val result = withContext(Dispatchers.IO) {
                    InnertubeApiService.getInstance(application)
                        .relatedPage(NextBody(videoId = relatedMediaId))
                }
                if (result?.isSuccess == true) {
                    val data = result.getOrNull()
                    if (data != null) {
                        _uiState.value = UiState.Success(data)
                    } else {
                        _uiState.value = UiState.Error
                    }
                } else {
                    _uiState.value = UiState.Error
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error
            } finally {
                isRefreshing.value = false
            }
        }
    }

    fun refresh() {
        if (isRefreshing.value) return
        fetchRelatedMedia()
    }
}