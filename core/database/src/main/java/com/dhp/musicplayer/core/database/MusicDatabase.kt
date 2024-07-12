package com.dhp.musicplayer.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dhp.musicplayer.core.database.dao.AlbumDao
import com.dhp.musicplayer.core.database.dao.ArtistDao
import com.dhp.musicplayer.core.database.dao.FavoriteDao
import com.dhp.musicplayer.core.database.dao.PlaylistDao
import com.dhp.musicplayer.core.database.dao.SearchHistoryDao
import com.dhp.musicplayer.core.database.dao.SongDao
import com.dhp.musicplayer.core.database.model.AlbumEntity
import com.dhp.musicplayer.core.database.model.ArtistEntity
import com.dhp.musicplayer.core.database.model.PlaylistEntity
import com.dhp.musicplayer.core.database.model.SearchHistoryEntity
import com.dhp.musicplayer.core.database.model.SongAlbumRelated
import com.dhp.musicplayer.core.database.model.SongArtistRelated
import com.dhp.musicplayer.core.database.model.SongEntity
import com.dhp.musicplayer.core.database.model.SongPlaylistMapEntity
import com.dhp.musicplayer.core.database.model.SongWithSongRelated
import com.dhp.musicplayer.core.database.model.SortedSongPlaylistMap

@Database(
    entities = [
        SongEntity::class,
        SongPlaylistMapEntity::class,
        PlaylistEntity::class,
        SearchHistoryEntity::class,
        SongWithSongRelated::class,
        AlbumEntity::class,
        SongAlbumRelated::class,
        ArtistEntity::class,
        SongArtistRelated::class
    ],
    views = [SortedSongPlaylistMap::class],
    version = 2,
    exportSchema = true
)
abstract class MusicDatabase : RoomDatabase() {
    abstract val songDao: SongDao
    abstract val albumDao: AlbumDao
    abstract val artistDao: ArtistDao
    abstract val playlistDao: PlaylistDao
    abstract val favoriteDao: FavoriteDao
    abstract val searchHistoryDao: SearchHistoryDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS SongWithSongRelated (" +
                    "songId TEXT NOT NULL, " +
                    "relatedSongId TEXT NOT NULL, " +
                    "PRIMARY KEY(songId, relatedSongId))"
        )

        db.execSQL("CREATE TABLE IF NOT EXISTS `Album` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `thumbnailUrl` TEXT, `year` TEXT, `authorsText` TEXT, PRIMARY KEY(`id`))")
        db.execSQL("CREATE TABLE IF NOT EXISTS `SongAlbumRelated` (`songId` TEXT NOT NULL, `albumId` TEXT NOT NULL, PRIMARY KEY(`songId`, `albumId`))")

        db.execSQL("CREATE TABLE IF NOT EXISTS `Artist` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `thumbnailUrl` TEXT, `subscribersCountText` TEXT, PRIMARY KEY(`id`))")
        db.execSQL("CREATE TABLE IF NOT EXISTS SongArtistRelated (songId TEXT NOT NULL, artistId TEXT NOT NULL, PRIMARY KEY(songId, artistId))")
    }
}