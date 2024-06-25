package com.dhp.musicplayer.core.services.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.DatabaseProvider
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import com.dhp.musicplayer.core.datastore.MaxSongCacheSizeKey
import com.dhp.musicplayer.core.datastore.dataStore
import com.dhp.musicplayer.core.datastore.get
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PlayerCache

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DownloadCache

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @OptIn(UnstableApi::class)
    @Singleton
    @Provides
    fun provideDatabaseProvider(@ApplicationContext context: Context): DatabaseProvider =
        StandaloneDatabaseProvider(context)

    @OptIn(UnstableApi::class)
    @Singleton
    @Provides
    @PlayerCache
    fun providePlayerCache(
        @ApplicationContext context: Context,
        databaseProvider: DatabaseProvider
    ): SimpleCache {
        val constructor = {
            SimpleCache(
                context.filesDir.resolve("exoplayercache"),
                when (val cacheSize = context.dataStore[MaxSongCacheSizeKey] ?: 1024) {
                    -1 -> NoOpCacheEvictor()
                    else -> LeastRecentlyUsedCacheEvictor(cacheSize * 1024 * 1024L)
                },
                databaseProvider
            )
        }
        constructor().release()
        return constructor()
    }

    @OptIn(UnstableApi::class)
    @Singleton
    @Provides
    @DownloadCache
    fun provideDownloadCache(
        @ApplicationContext context: Context,
        databaseProvider: DatabaseProvider
    ): SimpleCache {
        val constructor = {
            SimpleCache(context.filesDir.resolve("download"), NoOpCacheEvictor(), databaseProvider)
        }
        constructor().release()
        return constructor()
    }
}