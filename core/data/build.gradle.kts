plugins {
    alias(libs.plugins.dhp.android.library)
    alias(libs.plugins.dhp.android.hilt)
}

android {
    namespace = "com.dhp.musicplayer.core.datas"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:domain"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(libs.paging.runtime.ktx)

}