package com.dhp.musicplayer.core.network.di

import com.dhp.musicplayer.core.network.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.BrowserUserAgent
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class KuGouHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FeedbackHttpClient

@Module
@InstallIn(SingletonComponent::class)
class NetworkModules {
    private val baseUrl = BuildConfig.API_BASE_URL
    private val baseUrlFeedback = "docs.google.com/forms/d/e"

    @Singleton
    @Provides
    @AppHttpClient
    fun providesHttpClient() = HttpClient(OkHttp) {
        BrowserUserAgent()
        expectSuccess = true

        install(ContentNegotiation) {
            @OptIn(ExperimentalSerializationApi::class)
            json(Json {
                ignoreUnknownKeys = true
                explicitNulls = false
                encodeDefaults = true
            })
        }

        defaultRequest {
            url(scheme = "https", host = baseUrl) {
                contentType(ContentType.Application.Json)
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Singleton
    @Provides
    @KuGouHttpClient
    fun providesKuGouHttpClient() = HttpClient(OkHttp) {
        BrowserUserAgent()

        expectSuccess = true

        install(ContentNegotiation) {
            val feature = Json {
                ignoreUnknownKeys = true
                explicitNulls = false
                encodeDefaults = true
            }

            json(feature)
            json(feature, ContentType.Text.Html)
            json(feature, ContentType.Text.Plain)
        }

        install(ContentEncoding) {
            gzip()
            deflate()
        }

        defaultRequest {
            url("https://krcs.kugou.com")
        }
    }

    @Singleton
    @Provides
    @FeedbackHttpClient
    fun providesFeedbackHttpClient() = HttpClient(OkHttp) {
        BrowserUserAgent()
        expectSuccess = true

        install(ContentNegotiation) {
            @OptIn(ExperimentalSerializationApi::class)
            json(Json {
                ignoreUnknownKeys = true
                explicitNulls = false
                encodeDefaults = true
            })
        }

        defaultRequest {
            url(scheme = "https", host = baseUrlFeedback) {
                contentType(ContentType.Application.Json)
            }
        }
    }
}