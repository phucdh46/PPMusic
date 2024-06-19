plugins {
    alias(libs.plugins.dhp.android.library.compose)
    alias(libs.plugins.dhp.android.feature)
}

android {
    namespace = "com.dhp.musicplayer.feature.search"

}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":data:repository"))
    implementation(project(":feature:menu"))

    implementation(libs.paging.compose)
    implementation(libs.paging.runtime.ktx)
}