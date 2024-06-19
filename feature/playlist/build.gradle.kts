plugins {
    alias(libs.plugins.dhp.android.library.compose)
    alias(libs.plugins.dhp.android.feature)
}

android {
    namespace = "com.dhp.musicplayer.feature.playlist"

}

dependencies {
    implementation(project(":data:repository"))
    implementation(project(":feature:menu"))
    implementation(project(":core:common"))


}