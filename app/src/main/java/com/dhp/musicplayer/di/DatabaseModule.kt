package com.dhp.musicplayer.di

import android.content.Context
import androidx.room.Room
import com.dhp.musicplayer.db.MusicDao
import com.dhp.musicplayer.db.MusicDatabase
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
            .build()

    @Provides
    fun providesChatDao(database: MusicDatabase): MusicDao = database.musicDao
}