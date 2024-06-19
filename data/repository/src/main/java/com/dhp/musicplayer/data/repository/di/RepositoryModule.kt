package com.dhp.musicplayer.data.repository.di

import com.dhp.musicplayer.data.network.api.ApiService
import com.dhp.musicplayer.data.network.api.ApiServiceImpl
import com.dhp.musicplayer.data.repository.AppRepository
import com.dhp.musicplayer.data.repository.AppRepositoryImpl
import com.dhp.musicplayer.data.repository.MusicRepository
import com.dhp.musicplayer.data.repository.MusicRepositoryImpl
import com.dhp.musicplayer.data.repository.NetworkMusicRepository
import com.dhp.musicplayer.data.repository.NetworkMusicRepositoryImpl
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
    internal abstract fun bindsApiService(
        apiService: ApiServiceImpl,
    ): ApiService

    @Binds
    internal abstract fun bindsMusicRepository(musicRepository: MusicRepositoryImpl): MusicRepository

    @Binds
    internal abstract fun bindsNetworkMusicRepository(networkMusicRepository: NetworkMusicRepositoryImpl): NetworkMusicRepository
}