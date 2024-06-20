plugins {
    alias(libs.plugins.dhp.android.library)
    alias(libs.plugins.dhp.android.hilt)
}

android {
    namespace = "com.dhp.musicplayer.core.domain"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
//    implementation(project(":core:datas"))
//    implementation(libs.javax.inject)
    implementation(libs.paging.runtime.ktx)


}