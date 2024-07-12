package com.dhp.musicplayer.core.data.di

import com.dhp.musicplayer.core.data.firebase.FirebaseService
import com.dhp.musicplayer.core.data.firebase.FirebaseServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    internal abstract fun bindsFirebaseService(
        firebaseService: FirebaseServiceImpl,
    ): FirebaseService
}