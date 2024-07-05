plugins {
    alias(libs.plugins.dhp.android.library.compose)
    alias(libs.plugins.dhp.android.feature)
}

android {
    namespace = "com.dhp.musicplayer.feature.settings"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:datastore"))
    implementation(project(":core:data"))
    implementation(libs.coil.kt)
    implementation(libs.play.services.ads.lite)
}