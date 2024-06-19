plugins {
    alias(libs.plugins.dhp.android.library)
    alias(libs.plugins.dhp.android.library.compose)
}

android {
    namespace = "com.dhp.musicplayer.core.ui"

}

dependencies {

    api(project(":core:common"))
    api(project(":core:designsystem"))
    api(project(":core:model"))
    api(project(":core:services"))
    api(project(":data:datastore"))
    api(project(":data:network"))

    implementation(libs.coil.compose)

    implementation(libs.paging.compose)
    implementation(libs.paging.runtime.ktx)
    
//    implementation(libs.androidx.core.ktx)
//    implementation(libs.androidx.appcompat)
//    implementation(libs.material)
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.junit)
//    androidTestImplementation(libs.androidx.espresso.core)
}