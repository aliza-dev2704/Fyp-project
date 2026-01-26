plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.example.blindnavigator"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.blindnavigator"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // AndroidX & Material
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.11.0")

    // Google Play Services
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.android.gms:play-services-maps:18.1.0")

    // ML Kit - Object Detection & Image Labeling
    implementation("com.google.mlkit:object-detection:17.0.1")
    implementation("com.google.mlkit:image-labeling:17.0.7")
    implementation("com.google.mlkit:image-labeling-common:18.0.0")
    implementation("com.google.mlkit:vision-common:17.3.0")



    // ⚠️ Removed: com.google.mlkit:vision-label (does not exist)
    // ⚠️ Removed duplicate ML Kit dependencies
    // ⚠️ Removed play-services-vision (old, deprecated unless you specifically need it)

    // CameraX (for live camera feed)
    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")

    implementation("com.google.mlkit:image-labeling:17.0.7")
    implementation("com.google.mlkit:image-labeling-common:18.0.0")
    implementation("com.google.mlkit:vision-common:17.3.0")



}
