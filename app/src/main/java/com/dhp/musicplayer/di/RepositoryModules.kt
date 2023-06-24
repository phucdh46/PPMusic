package com.dhp.musicplayer.di

import com.dhp.musicplayer.repository.LoginsRepository
import com.dhp.musicplayer.repository.LoginsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModules {
    @Binds
    abstract fun provideLoginsRepository(loginsRepositoryImpl: LoginsRepositoryImpl): LoginsRepository
}