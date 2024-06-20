plugins {
    alias(libs.plugins.dhp.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.jetbrainsKotlinSerialization)
    alias(libs.plugins.dhp.android.hilt)
}

android {
    namespace = "com.dhp.musicplayer.core.database"

}

dependencies {
    ksp(libs.room.compiler)
    implementation(libs.room.ktx)
    implementation(libs.kotlinx.serialization.json)

}