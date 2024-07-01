package com.dhp.musicplayer.feature.library.songs.device_songs

import android.app.Application
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhp.musicplayer.core.common.enums.UiState
import com.dhp.musicplayer.core.common.extensions.bitMapToString
import com.dhp.musicplayer.core.common.extensions.toContentUri
import com.dhp.musicplayer.core.model.music.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class DeviceSongsViewModel @Inject constructor(
    private val application: Application,
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<Song>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Song>>> get() = _uiState

    fun getDeviceMusic() {
        viewModelScope.launch(Dispatchers.IO) {
            if (_uiState.value !is UiState.Success) {
                val deviceSongs = queryForMusic()
                if (deviceSongs != null) {
                    if (_uiState.value != UiState.Success(data = deviceSongs)) {
                        _uiState.value = UiState.Success(data = deviceSongs)
                    }
                } else {
                    _uiState.value = UiState.Error
                }
            }
        }
    }

    private fun queryForMusic() =
        try {
            val projection = arrayOf(
                MediaStore.Audio.AudioColumns.ARTIST, // 0
                MediaStore.Audio.AudioColumns.TITLE, // 3
                MediaStore.Audio.AudioColumns.DURATION, //5,
                MediaStore.Audio.AudioColumns._ID, // 9
            )
            val selection = "${MediaStore.Audio.AudioColumns.IS_MUSIC} = 1"
            val sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER

            val musicCursor = application.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )
            val mDeviceMusicList = mutableListOf<Song>()

            // Query the storage for music files
            musicCursor?.use { cursor ->
                val artistIndex =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST)
                val titleIndex =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TITLE)
                val durationIndex =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION)
                val idIndex =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns._ID)

                while (cursor.moveToNext()) {
                    // Now loop through the music files
                    val audioId = cursor.getLong(idIndex)
                    val audioArtist = cursor.getString(artistIndex)
                    val audioTitle = cursor.getString(titleIndex)
                    val audioDuration = cursor.getLong(durationIndex)

                    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        try {
                            application.contentResolver.loadThumbnail(
                                audioId.toContentUri(), Size(640, 480), null
                            )
                        } catch (e: IOException) {
                            null
                        }
                    } else {
                        null
                    }
                    val thumbnailUrl = bitmap?.let { bitMapToString(it) }

                    // Add the current music to the list
                    mDeviceMusicList.add(
                        Song(
                            id = audioId.toString(),
                            idLocal = audioId,
                            title = audioTitle,
                            artistsText = audioArtist,
                            durationText = audioDuration.toString(),
                            thumbnailUrl = thumbnailUrl,
                            isOffline = true
                        )
                    )
                }
            }
            Log.d("DHP", "mDeviceMusicList: $mDeviceMusicList")
            mDeviceMusicList
        } catch (e: Exception) {
//            e.printStackTrace()
            null
        }
}