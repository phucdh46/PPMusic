package com.dhp.musicplayer.feature.settings.feedback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhp.musicplayer.core.domain.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val appRepository: AppRepository
) : ViewModel() {
    private val _message: MutableStateFlow<String?> = MutableStateFlow(null)
    val message = _message.asStateFlow()

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun sendFeedback(feedback: String, email: String, name: String) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val result = appRepository.sendFeedback(feedback, email, name)
            _message.value = if (result) "Feedback sent successfully" else "Failed to send feedback"
            _isLoading.value = false
        }
    }
}