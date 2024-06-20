plugins {
    alias(libs.plugins.dhp.android.library)
    alias(libs.plugins.dhp.android.hilt)
    alias(libs.plugins.jetbrainsKotlinSerialization)
}

android {
    namespace = "com.dhp.musicplayer.core.network"
}

dependencies {
    api(project(":core:common"))
    implementation(project(":core:model"))
    api(project(":core:datastore"))

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.encoding)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.brotli)

    implementation(libs.paging.runtime.ktx)
}