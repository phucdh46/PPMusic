plugins {
    alias(libs.plugins.dhp.android.library)
//    alias(libs.plugins.dhp.android.library.compose)
//    alias(libs.plugins.ksp)
}

android {
    namespace = "com.dhp.musicplayer.data.datastore"
}

dependencies {
    implementation(project(":core:common"))
//    implementation(libs.androidx.lifecycle.runtimeCompose)

    api(libs.androidx.datastore.preferences)
    api(libs.androidx.dataStore.core)

//    implementation(libs.androidx.core.ktx)
//    implementation(libs.androidx.appcompat)
//    implementation(libs.material)
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.junit)
//    androidTestImplementation(libs.androidx.espresso.core)
}