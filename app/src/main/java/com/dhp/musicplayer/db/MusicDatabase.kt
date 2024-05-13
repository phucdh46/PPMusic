package com.dhp.musicplayer.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dhp.musicplayer.model.Playlist
import com.dhp.musicplayer.model.SearchHistory
import com.dhp.musicplayer.model.Song
import com.dhp.musicplayer.model.SongPlaylistMap
import com.dhp.musicplayer.model.SortedSongPlaylistMap

@Database(
    entities = [
        Song::class,
        SongPlaylistMap::class,
        Playlist::class,
        SearchHistory::class
    ],
    views = [SortedSongPlaylistMap::class],
    version = 1,
)
abstract class MusicDatabase : RoomDatabase() {
    abstract val musicDao: MusicDao
}