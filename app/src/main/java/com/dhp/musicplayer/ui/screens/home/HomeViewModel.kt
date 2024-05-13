package com.dhp.musicplayer.ui.screens.home

import android.app.Application
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhp.musicplayer.R
import com.dhp.musicplayer.extensions.isAtLeastAndroid29
import com.dhp.musicplayer.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val application: Application
): ViewModel() {

    private val _deviceMusic = MutableLiveData<MutableList<Song>?>()
    val deviceMusic: LiveData<MutableList<Song>?> = _deviceMusic

    init {
        getDeviceMusic()
    }
    private fun getDeviceMusic() {
        viewModelScope.launch(Dispatchers.IO) {
            _deviceMusic.postValue(queryForMusic())
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

            val musicCursor = application.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, sortOrder)

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
                            artistsText =  audioArtist,
                            durationText = audioDuration.toString(),
                            thumbnailUrl = audioFolderName,
                            isOffline = true
                        )
//                        Song(
//                            audioArtist,
//                            audioYear,
//                            audioTrack,
//                            audioTitle,
//                            audioDisplayName,
//                            audioDuration,
//                            audioAlbum,
//                            albumId,
//                            audioFolderName,
//                            audioId,
//                            "0",
//                            0,
//                            audioDateAdded
//                        )
                    )
                }
            }
            Log.d("DHP","mDeviceMusicList: $mDeviceMusicList")
            mDeviceMusicList
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

}