package com.dhp.musicplayer.core.database.di

import android.content.Context
import androidx.room.Room
import com.dhp.musicplayer.core.database.MIGRATION_1_2
import com.dhp.musicplayer.core.database.MusicDatabase
import com.dhp.musicplayer.core.database.dao.AlbumDao
import com.dhp.musicplayer.core.database.dao.ArtistDao
import com.dhp.musicplayer.core.database.dao.FavoriteDao
import com.dhp.musicplayer.core.database.dao.PlaylistDao
import com.dhp.musicplayer.core.database.dao.SearchHistoryDao
import com.dhp.musicplayer.core.database.dao.SongDao

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providesAppDatabase(@ApplicationContext context: Context): MusicDatabase =
        Room.databaseBuilder(context, MusicDatabase::class.java, "music.db")
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    fun providesSongDao(database: MusicDatabase): SongDao = database.songDao

    @Provides
    fun providesAlbumDao(database: MusicDatabase): AlbumDao = database.albumDao

    @Provides
    fun providesArtistDao(database: MusicDatabase): ArtistDao = database.artistDao

    @Provides
    fun providesPlaylistDao(database: MusicDatabase): PlaylistDao = database.playlistDao

    @Provides
    fun providesFavoriteDao(database: MusicDatabase): FavoriteDao = database.favoriteDao

    @Provides
    fun providesSearchHistoryDao(database: MusicDatabase): SearchHistoryDao = database.searchHistoryDao
}