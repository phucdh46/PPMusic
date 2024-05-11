package com.dhp.musicplayer.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dhp.musicplayer.models.Song
import com.dhp.musicplayer.models.SongPlaylistMap
import com.dhp.musicplayer.models.Playlist
import com.dhp.musicplayer.models.SortedSongPlaylistMap

@Database(
    entities = [
        Song::class,
        SongPlaylistMap::class,
        Playlist::class,
    ],
    views = [SortedSongPlaylistMap::class],
    version = 1,
)
abstract class MusicDatabase : RoomDatabase() {
    abstract val musicDao: MusicDao
}