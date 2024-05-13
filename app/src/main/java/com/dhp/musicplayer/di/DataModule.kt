package com.dhp.musicplayer.di

import com.dhp.musicplayer.repository.MusicRepository
import com.dhp.musicplayer.repository.MusicRepositoryImpl
import com.dhp.musicplayer.repository.UserDataRepository
import com.dhp.musicplayer.repository.UserDataRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    internal abstract fun bindsUserDataRepository(
        userDataRepository: UserDataRepositoryImpl,
    ): UserDataRepository

    @Binds
    internal abstract fun bindsMusicRepository(
        musicRepository: MusicRepositoryImpl,
    ): MusicRepository
}