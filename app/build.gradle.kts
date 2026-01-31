
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    // If you are using Firebase via google-services plugin, add it here:
    // id("com.google.gms.google-services")
}

android {
    namespace = "com.runanywhere.startup_hackathon20"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.runanywhere.startup_hackathon20"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

        // Needed for java.time on minSdk < 26
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // -----------------------------
    // RunAnywhere SDK - Local AARs
    // -----------------------------
    implementation(files("libs/RunAnywhereKotlinSDK-release.aar"))
    implementation(files("libs/runanywhere-llm-llamacpp-release.aar"))

    // Desugaring (java.time etc.)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // -----------------------------
    // Kotlin / Networking (kept as you had)
    // -----------------------------
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")

    implementation("io.ktor:ktor-client-core:3.0.3")
    implementation("io.ktor:ktor-client-okhttp:3.0.3")
    implementation("io.ktor:ktor-client-content-negotiation:3.0.3")
    implementation("io.ktor:ktor-client-logging:3.0.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.3")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.squareup.okio:okio:3.9.1")

    // -----------------------------
    // CameraX (keep ONE version only)
    // -----------------------------
    val cameraxVersion = "1.4.1"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")

    // Fixes ListenableFuture / addListener issues
    implementation("androidx.concurrent:concurrent-futures-ktx:1.1.0")

    // ML Kit Text Recognition
    implementation("com.google.mlkit:text-recognition:16.0.1")

    // -----------------------------
    // Firebase + Google Auth
    // -----------------------------
    implementation("com.google.firebase:firebase-auth-ktx:23.0.0")
    implementation("com.google.android.gms:play-services-auth:21.1.0")

    // -----------------------------
    // Room (IMPORTANT: compiler must be ksp, NOT implementation)
    // -----------------------------
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // -----------------------------
    // WorkManager
    // -----------------------------
    implementation("androidx.work:work-runtime-ktx:2.10.0")

    // Security Crypto
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Core KTX (only once)
    implementation("androidx.core:core-ktx:1.13.1")

    // Navigation Compose (only one version)
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // Lifecycle / ViewModel Compose
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // -----------------------------
    // Compose (keep BOM + libs)
    // -----------------------------
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)

    // Icons
    implementation("androidx.compose.material:material-icons-extended:1.5.4")

    // -----------------------------
    // Testing
    // -----------------------------
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
