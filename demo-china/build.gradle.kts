plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.bosechina.jambi"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.bosechina.jambi"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0-china"
        buildConfigField("String", "UMENG_APPKEY", "\"6a71ead6934d206f5852c9ab\"")
        buildConfigField("String", "UMENG_CHANNEL", "\"demo\"")
    }

    buildTypes {
        release { isMinifyEnabled = false }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":crash-kit-china"))
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
}
