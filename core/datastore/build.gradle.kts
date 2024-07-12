plugins {
    alias(libs.plugins.dhp.android.library)
    alias(libs.plugins.jetbrainsKotlinSerialization)
}

android {
    namespace = "com.dhp.musicplayer.core.datastore"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
//    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.kotlinx.serialization.json)

    api(libs.androidx.datastore.preferences)
    api(libs.androidx.dataStore.core)
}