plugins {
    alias(libs.plugins.dhp.android.library)
    alias(libs.plugins.dhp.android.hilt)
}

android {
    namespace = "com.dhp.musicplayer.core.billing"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:datastore"))
    implementation(libs.billing.ktx)
}