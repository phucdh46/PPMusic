plugins {
    alias(libs.plugins.dhp.android.library.compose)
    alias(libs.plugins.dhp.android.feature)
}

android {
    namespace = "com.dhp.musicplayer.feature.home"

}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":data:repository"))
    implementation(project(":data:network"))
    implementation(project(":feature:menu"))

    implementation(libs.accompanist.swiperefresh)

}