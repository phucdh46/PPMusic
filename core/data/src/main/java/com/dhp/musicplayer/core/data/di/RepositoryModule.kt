//package com.dhp.musicplayer.core.datas.di
//
//import com.dhp.musicplayer.core.datas.repository.AppRepositoryImpl
//import com.dhp.musicplayer.core.domain.repository.AppRepository
//import dagger.Binds
//import dagger.Module
//import dagger.hilt.EntryPoint
//import dagger.hilt.InstallIn
//import dagger.hilt.components.SingletonComponent
//
//@Module
//@InstallIn(SingletonComponent::class)
//abstract class RepositoryModule {
//    @Binds
//    internal abstract fun bindsAppRepository(
//        musicRepository: AppRepositoryImpl,
//    ): AppRepository
//
////    @Binds
////    internal abstract fun bindsApiService(
////        apiService: ApiServiceImpl,
////    ): ApiService
////
////    @Binds
////    internal abstract fun bindsMusicRepository(musicRepository: MusicRepositoryImpl): MusicRepository
////
////    @Binds
////    internal abstract fun bindsNetworkMusicRepository(networkMusicRepository: NetworkMusicRepositoryImpl): NetworkMusicRepository
//}