plugins {
    alias(libs.plugins.dhp.android.library.compose)
    alias(libs.plugins.dhp.android.feature)
}

android {
    namespace = "com.dhp.musicplayer.feature.library"

}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":feature:menu"))
    implementation(libs.accompanist.permissions)

}