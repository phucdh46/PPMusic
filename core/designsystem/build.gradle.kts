plugins {
    alias(libs.plugins.dhp.android.library)
    alias(libs.plugins.dhp.android.library.compose)
}

android {
    namespace = "com.dhp.musicplayer.core.designsystem"

}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.foundation.layout)
    api(libs.androidx.material.icons.extended)
    api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.ui.util)
    api(libs.androidx.compose.material3.android)

    implementation(libs.coil.kt)
    implementation(libs.palette)
    implementation(libs.androidx.activity.compose)

    testImplementation(libs.androidx.ui.test.junit4)
//    testImplementation(libs.hilt.android.testing)
//    testImplementation(libs.robolectric)
//    testImplementation(libs.roborazzi)
//    testImplementation(projects.core.screenshotTesting)
//    testImplementation(projects.core.testing)

    androidTestImplementation(libs.androidx.ui.test.junit4)
//    androidTestImplementation(projects.core.testing)
}