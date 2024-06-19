package com.dhp.musicplayer.feature.library.songs.device_songs

import android.app.Application
import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhp.musicplayer.core.common.enums.UiState
import com.dhp.musicplayer.core.common.extensions.bitMapToString
import com.dhp.musicplayer.core.common.extensions.isAtLeastAndroid29
import com.dhp.musicplayer.core.model.music.Song
import com.dhp.musicplayer.core.services.extensions.toContentUri
import com.dhp.musicplayer.feature.library.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class DeviceSongsViewModel @Inject constructor(
    private val application: Application
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
            val pathColumn = if (isAtLeastAndroid29) {
                MediaStore.Audio.AudioColumns.BUCKET_DISPLAY_NAME
            } else {
                MediaStore.Audio.AudioColumns.DATA
            }
            var mDeviceMusicList = mutableListOf<Song>()
            val projection = arrayOf(
                MediaStore.Audio.AudioColumns.ARTIST, // 0
                MediaStore.Audio.AudioColumns.YEAR, // 1
                MediaStore.Audio.AudioColumns.TRACK, // 2
                MediaStore.Audio.AudioColumns.TITLE, // 3
                MediaStore.Audio.AudioColumns.DISPLAY_NAME, // 4,
                MediaStore.Audio.AudioColumns.DURATION, //5,
                MediaStore.Audio.AudioColumns.ALBUM, // 6
                MediaStore.Audio.AudioColumns.ALBUM_ID, // 7
                pathColumn, // 8
                MediaStore.Audio.AudioColumns._ID, // 9
                MediaStore.MediaColumns.DATE_MODIFIED // 10
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

            // Query the storage for music files
            musicCursor?.use { cursor ->

                val artistIndex =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST)
                val yearIndex =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.YEAR)
                val trackIndex =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TRACK)
                val titleIndex =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TITLE)
                val displayNameIndex =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DISPLAY_NAME)
                val durationIndex =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION)
                val albumIndex =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM)
                val albumIdIndex =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM_ID)
                val relativePathIndex =
                    cursor.getColumnIndexOrThrow(pathColumn)
                val idIndex =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns._ID)
                val dateAddedIndex =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DATE_MODIFIED)

                while (cursor.moveToNext()) {
                    // Now loop through the music files
                    val audioId = cursor.getLong(idIndex)
                    val audioArtist = cursor.getString(artistIndex)
                    val audioYear = cursor.getInt(yearIndex)
                    val audioTrack = cursor.getInt(trackIndex)
                    val audioTitle = cursor.getString(titleIndex)
                    val audioDisplayName = cursor.getString(displayNameIndex)
                    val audioDuration = cursor.getLong(durationIndex)
                    val audioAlbum = cursor.getString(albumIndex)
                    val albumId = cursor.getLong(albumIdIndex)
                    val audioRelativePath = cursor.getString(relativePathIndex)
                    val audioDateAdded = cursor.getInt(dateAddedIndex)
                    val contentUri: Uri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        audioId
                    )
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

                    val audioFolderName =
                        if (isAtLeastAndroid29) {
                            audioRelativePath ?: application.getString(R.string.slash)
                        } else {
                            var returnedPath = File(audioRelativePath).parentFile?.name
                            if (returnedPath == null || returnedPath == "0") {
                                returnedPath = application.getString(R.string.slash)
                            }
                            returnedPath
                        }

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