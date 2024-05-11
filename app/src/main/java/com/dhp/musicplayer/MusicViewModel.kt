package com.dhp.musicplayer

import android.app.Application
import android.content.res.Resources
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.dhp.musicplayer.models.Album
import com.dhp.musicplayer.models.Music
import com.dhp.musicplayer.utils.MusicUtils
import com.dhp.musicplayer.utils.Versioning
import kotlinx.coroutines.*
import java.io.File
import kotlin.random.Random

class MusicViewModel(application: Application): AndroidViewModel(application) {
    private val mViewModelJob = SupervisorJob()

    private val mHandler = CoroutineExceptionHandler { _, exception ->
        exception.printStackTrace()
        deviceMusic.value = null
    }

    private val mUiDispatcher = Dispatchers.Main
    private val mIoDispatcher = Dispatchers.IO + mViewModelJob + mHandler
    private val mUiScope = CoroutineScope(mUiDispatcher)

    val deviceMusic = MutableLiveData<MutableList<Music>?>()

    private var mDeviceMusicList = mutableListOf<Music>()
    var deviceMusicFiltered: MutableList<Music>? = null

    //keys: artist || value: its songs
    var deviceSongsByArtist: Map<String?, List<Music>>? = null

    //keys: album || value: its songs
    var deviceMusicByAlbum: Map<String?, List<Music>>? = null

    //keys: artist || value: albums
    var deviceAlbumsByArtist: MutableMap<String, List<Album>>? = mutableMapOf()

    //keys: artist || value: songs contained in the folder
    var deviceMusicByFolder: Map<String, List<Music>>? = null

    fun getDeviceMusic() {
        mUiScope.launch {
            withContext(mIoDispatcher) {
                val music = getMusic(getApplication()) // get music from MediaStore on IO thread
                withContext(mUiDispatcher) {
                    deviceMusic.value = music // post values on Main thread
                }
            }
        }
    }

    private fun getMusic(application: Application): MutableList<Music> {
        synchronized(startQuery(application)) {
            if (mDeviceMusicList.isNotEmpty()) {
                buildLibrary(application.resources)
            }
        }
        return mDeviceMusicList
    }

    private fun buildLibrary(resources: Resources) {
        // Removing duplicates by comparing everything except path which is different
        // if the same song is hold in different paths
        deviceMusicFiltered =
            mDeviceMusicList.distinctBy { it.artist to it.year to it.track to it.title to it.duration to it.album }
                .toMutableList()

        deviceMusicFiltered?.let { dsf ->
            //dsf.filterNot { Preferences.getPrefsInstance().filters?.contains(it.artist)!!}
            // group music by artist
            deviceSongsByArtist = dsf.groupBy { it.artist }
            deviceMusicByAlbum = dsf.groupBy { it.album }
            deviceMusicByFolder = dsf.groupBy { it.relativePath!! }
        }

        // group artists songs by albums
        deviceSongsByArtist?.keys?.iterator()?.let { iterate ->
            while (iterate.hasNext()) {
                iterate.next()?.let { artistKey ->
                    val album = deviceSongsByArtist?.getValue(artistKey)
                    deviceAlbumsByArtist?.set(
                        artistKey, MusicUtils.buildSortedArtistAlbums(resources, album)
                    )
                }
            }
        }

        updatePreferences()
    }

    private fun updatePreferences() {
        val prefs = Preferences.getPrefsInstance()

        if (prefs.latestPlayedSong == null) {
            prefs.latestPlayedSong = getRandomMusic()
        }
    }

    fun getRandomMusic(): Music? {
        deviceMusicFiltered?.shuffled()?.run {
            return get(Random.nextInt(size))
        }
        return deviceMusicFiltered?.random()
    }

    private fun startQuery(application: Application) {
        queryForMusic(application)?.let { fm ->
            mDeviceMusicList = fm
        }
    }

    @Suppress("DEPRECATION")
    fun queryForMusic(application: Application) =

        try {
            val pathColumn = if (Versioning.isQ()) {
                MediaStore.Audio.AudioColumns.BUCKET_DISPLAY_NAME
            } else {
                MediaStore.Audio.AudioColumns.DATA
            }

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

                    val audioFolderName =
                        if (Versioning.isQ()) {
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
                        Music(
                            audioArtist,
                            audioYear,
                            audioTrack,
                            audioTitle,
                            audioDisplayName,
                            audioDuration,
                            audioAlbum,
                            albumId,
                            audioFolderName,
                            audioId,
                            "0",
                            0,
                            audioDateAdded
                        )
                    )
                }
            }
            mDeviceMusicList
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
}