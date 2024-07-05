import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.hilt)
    alias(libs.plugins.jetbrainsKotlinSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.kotlin.parcelize)

}

android {
    namespace = "com.dhp.musicplayer"
    compileSdk = 34

    signingConfigs {
        val keystorePropertiesFile = rootProject.file("keystore.properties")
        val keystoreProperties = Properties()
        keystoreProperties.load(FileInputStream(keystorePropertiesFile))
        create("release") {
            keyAlias = keystoreProperties["keyAlias"].toString()
            keyPassword = keystoreProperties["keyPassword"].toString()
            storeFile = file("keystore.jks")
            storePassword = keystoreProperties["storePassword"].toString()
        }
    }

    defaultConfig {
        applicationId = "com.dhp.musicplayer"
        minSdk = 26
        targetSdk = 34
        versionCode = 1000632
        versionName = "1.0.632"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        //load the values from .properties file
        val keystoreFile = project.rootProject.file("local.properties")
        val properties = Properties()
        properties.load(keystoreFile.inputStream())

        //return empty key in case something goes wrong
        val adsAppId = properties.getProperty("GMS_ADS_APP_ID") ?: ""
        resValue("string", "GMS_ADS_APP_ID", adsAppId)
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            manifestPlaceholders["appName"] = "Debug"
        }

        release {
            manifestPlaceholders["appName"] = "PPMusic"
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isDebuggable = false
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        freeCompilerArgs += "-Xcontext-receivers"
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    lint {
        disable.add("Instantiatable")
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:services"))
    implementation(project(":core:ui"))
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:network"))
    implementation(project(":core:datastore"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:home"))
    implementation(project(":feature:artist"))
    implementation(project(":feature:playlist"))
    implementation(project(":feature:search"))
    implementation(project(":feature:library"))
    implementation(project(":feature:player"))
//    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(libs.androidx.core.ktx)
//    implementation(libs.androidx.lifecycle.runtime.ktx)
//    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
//    implementation(libs.androidx.compose.material3.android)
//    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
//    implementation(libs.androidx.lifecycle.runtimeCompose)
//    implementation(libs.coil.compose)
//    implementation(libs.runtime.livedata)

    implementation(libs.hilt.android)
//    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

//    implementation(libs.accompanist.permissions)
//    implementation(libs.accompanist.pager)
//    implementation(libs.accompanist.swiperefresh)

    implementation(libs.coil.kt)
    implementation(libs.kotlinx.serialization.json)
//    implementation(libs.media3.exoplayer)
//    implementation(libs.media3.okhttp)
//    implementation(libs.media3.session)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    implementation(libs.navigation.fragment)

    implementation(libs.androidx.core.splashscreen)
    implementation(libs.app.update)
    implementation(libs.app.update.ktx)
//    implementation(libs.reorderable)

//    implementation(libs.paging.compose)
//    implementation(libs.paging.runtime.ktx)

//    implementation(libs.palette)
    implementation(libs.play.services.ads.lite)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
