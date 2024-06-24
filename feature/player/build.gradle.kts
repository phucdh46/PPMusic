plugins {
    alias(libs.plugins.dhp.android.library.compose)
    alias(libs.plugins.dhp.android.feature)
}

android {
    namespace = "com.dhp.musicplayer.feature.player"
}

dependencies {
    api(project(":core:services"))
    implementation(project(":core:domain"))
    implementation(project(":feature:menu"))
    implementation(project(":core:network"))
    implementation(libs.sh.reorderable)
    implementation(libs.androidx.constraintlayout.compose)
}