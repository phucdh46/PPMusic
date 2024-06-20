package com.dhp.musicplayer.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dhp.musicplayer.core.database.dao.FavoriteDao
import com.dhp.musicplayer.core.database.dao.PlaylistDao
import com.dhp.musicplayer.core.database.dao.SearchHistoryDao
import com.dhp.musicplayer.core.database.dao.SongDao
import com.dhp.musicplayer.core.database.model.PlaylistEntity
import com.dhp.musicplayer.core.database.model.SearchHistoryEntity
import com.dhp.musicplayer.core.database.model.SongEntity
import com.dhp.musicplayer.core.database.model.SongPlaylistMapEntity
import com.dhp.musicplayer.core.database.model.SortedSongPlaylistMap

@Database(
    entities = [
        SongEntity::class,
        SongPlaylistMapEntity::class,
        PlaylistEntity::class,
        SearchHistoryEntity::class
    ],
    views = [SortedSongPlaylistMap::class],
    version = 1,
)
abstract class MusicDatabase : RoomDatabase() {
    abstract val songDao: SongDao
    abstract val playlistDao: PlaylistDao
    abstract val favoriteDao: FavoriteDao
    abstract val searchHistoryDao: SearchHistoryDao
}