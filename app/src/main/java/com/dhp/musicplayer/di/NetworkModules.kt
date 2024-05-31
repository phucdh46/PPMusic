package com.dhp.musicplayer.di

import android.util.Log
import com.dhp.musicplayer.api.ApiMusicService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModules {
    private val BASE_URL = "https://ktor-sample-production-6f10.up.railway.app/"
//    private val BASE_URL = "ktor-sample-production-6f10.up.railway.app"

    @Singleton
    @Provides
    fun providesHttpClient() = HttpClient(OkHttp) {
//        BrowserUserAgent()

//        expectSuccess = true

        install(ContentNegotiation) {
            @OptIn(ExperimentalSerializationApi::class)
            json()
//            Json {
////                ignoreUnknownKeys = true
////                explicitNulls = false
////                encodeDefaults = true
//            }
        }
//        install(ContentEncoding) {
//            brotli()
//        }

        defaultRequest {
            url(scheme = "https", host = BASE_URL) {
//                headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
//                parameters.append("prettyPrint", "false")

            }
        }

    }

    @Singleton
    @Provides
    fun providesHttpLoggingInterceptor() = HttpLoggingInterceptor()
        .apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    @Singleton
    @Provides
    fun providesOkHttpClient(httpLoggingInterceptor: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient
            .Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(httpLoggingInterceptor)
            .build()

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient) : Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .build()

    @Singleton
    @Provides
    fun provideApiService(retrofit: Retrofit): ApiMusicService = retrofit.create(ApiMusicService::class.java)


}