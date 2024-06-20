package com.dhp.musicplayer.di

import com.dhp.musicplayer.core.data.repository.AppRepositoryImpl
import com.dhp.musicplayer.core.data.repository.MusicRepositoryImpl
import com.dhp.musicplayer.core.data.repository.NetworkMusicRepositoryImpl
import com.dhp.musicplayer.core.domain.repository.AppRepository
import com.dhp.musicplayer.core.domain.repository.MusicRepository
import com.dhp.musicplayer.core.domain.repository.NetworkMusicRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    internal abstract fun bindsAppRepository(
        musicRepository: AppRepositoryImpl,
    ): AppRepository

    @Binds
    internal abstract fun bindsMusicRepository(
        musicRepository: MusicRepositoryImpl
    ): MusicRepository

    @Binds
    internal abstract fun bindsNetworkMusicRepository(
        networkMusicRepository: NetworkMusicRepositoryImpl
    ): NetworkMusicRepository
}