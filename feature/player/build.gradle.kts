plugins {
    alias(libs.plugins.dhp.android.library.compose)
    alias(libs.plugins.dhp.android.feature)
}

android {
    namespace = "com.dhp.musicplayer.feature.player"

}

dependencies {

    api(project(":core:services"))
    api(project(":data:repository"))
    implementation(project(":feature:menu"))
    implementation(libs.accompanist.pager)
    implementation(libs.reorderable)

}