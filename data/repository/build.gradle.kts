plugins {
    alias(libs.plugins.dhp.android.library)
//    alias(libs.plugins.android.library)
    alias(libs.plugins.dhp.android.hilt)
}

android {
    namespace = "com.dhp.musicplayer.data.repository"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":data:network"))
    implementation(project(":data:database"))

//    implementation(libs.androidx.core.ktx)
//    implementation(libs.androidx.appcompat)
//    implementation(libs.material)
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.junit)
//    androidTestImplementation(libs.androidx.espresso.core)
}