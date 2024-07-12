package com.dhp.musicplayer.core.network.di

import com.dhp.musicplayer.core.network.api.ApiService
import com.dhp.musicplayer.core.network.api.ApiServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ApiModule {
    @Binds
    internal abstract fun bindsApiService(
        apiService: ApiServiceImpl,
    ): ApiService

}