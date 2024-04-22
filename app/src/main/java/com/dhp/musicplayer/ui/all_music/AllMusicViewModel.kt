package com.dhp.musicplayer.ui.all_music

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhp.musicplayer.Innertube
import com.dhp.musicplayer.api.isSuccess
import com.dhp.musicplayer.innnertube.PlayerResponse
import com.dhp.musicplayer.innnertube.runCatchingNonCancellable
import com.dhp.musicplayer.repository.Repository
import com.dhp.musicplayer.utils.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllMusicViewModel @Inject constructor( val repository: Repository)  : ViewModel() {
    private val relatedLiveData = MutableLiveData<Innertube.RelatedPage?>()
    private val playerLiveData = MutableLiveData<PlayerResponse?>()

    fun getRelated() = relatedLiveData
    fun getPlayer() = playerLiveData

    init {
        loadRelated()
    }

    suspend fun getPlayers(id: String) = runCatchingNonCancellable {
        repository.getPlayer(id)
    }

    private fun loadRelated() {
        viewModelScope.launch(Dispatchers.IO) {
            val related = repository.getRelated("yGi1MePEN-k")
            when (related.isSuccess()) {
                true -> {
                    relatedLiveData.postValue(related.result)
                }
                else -> {
                    Log.d("Error")
                }
            }
        }
    }

    private fun loadPlayer(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val players = repository.getPlayer(id)
            when (players.isSuccess()) {
                true -> {
                    playerLiveData.postValue(players.result)
                }
                else -> {
                    Log.d("Error")
                }
            }
        }
    }
}