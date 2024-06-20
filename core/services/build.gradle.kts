plugins {
    alias(libs.plugins.dhp.android.library)
    alias(libs.plugins.dhp.android.hilt)
    alias(libs.plugins.jetbrainsKotlinSerialization)

}

android {
    namespace = "com.dhp.musicplayer.core.services"
    kotlinOptions {
        freeCompilerArgs += "-Xcontext-receivers"
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:domain"))
    implementation(project(":core:datastore"))
    implementation(project(":core:network"))

    api(libs.media3.exoplayer)
    implementation(libs.media3.okhttp)
    implementation(libs.media3.session)

    implementation(libs.kotlinx.serialization.json)

//    implementation(libs.androidx.core.ktx)
//    implementation(libs.androidx.appcompat)
//    implementation(libs.material)
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.junit)
//    androidTestImplementation(libs.androidx.espresso.core)
}