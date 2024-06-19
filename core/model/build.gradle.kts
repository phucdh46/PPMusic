plugins {
    alias(libs.plugins.dhp.android.library)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.jetbrainsKotlinSerialization)
}

android {
    namespace = "com.dhp.musicplayer.core.model"
}

dependencies {
    implementation(libs.kotlinx.serialization.json)

//    implementation(libs.androidx.core.ktx)
//    implementation(libs.androidx.appcompat)
//    implementation(libs.material)
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.junit)
//    androidTestImplementation(libs.androidx.espresso.core)
}