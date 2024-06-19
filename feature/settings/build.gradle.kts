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
    implementation(project(":data:datastore"))

//    implementation(libs.androidx.hilt.navigation.compose)

//    implementation(libs.androidx.core.ktx)
//    implementation(libs.androidx.appcompat)
//    implementation(libs.material)
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.junit)
//    androidTestImplementation(libs.androidx.espresso.core)
}