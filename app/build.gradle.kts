import java.util.Properties // Pastikan impor ini ada di paling atas

plugins {
    alias(libs.plugins.android.application)
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose") // Plugin Compose Compiler, penting!
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"
    id("com.google.devtools.ksp")
}

// Memuat local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists() && localPropertiesFile.isFile) {
    try {
        localPropertiesFile.inputStream().use { stream ->
            localProperties.load(stream)
        }
        println("SUCCESS: local.properties loaded successfully.")
    } catch (e: Exception) {
        println("ERROR: Failed to load local.properties: ${e.message}")
        // throw GradleException("Failed to load local.properties: ${e.message}") // Aktifkan jika local.properties wajib
    }
} else {
    println("INFO: local.properties file not found. OPEN_WEATHER_API_KEY will be empty in BuildConfig.")
}

android {
    // PASTIKAN namespace ini SAMA PERSIS dengan package di AndroidManifest.xml
    // dan deklarasi package di file Kotlin Anda (misal, MainActivity.kt)
    namespace = "com.example.elysia_assistant"
    compileSdk = 35 // Menggunakan versi SDK stabil (Android 14)

    defaultConfig {
        // PASTIKAN applicationId ini SAMA PERSIS dengan package di AndroidManifest.xml
        // dan namespace di atas jika Anda ingin konsisten.
        applicationId = "com.example.elysia_assistant"
        minSdk = 24
        targetSdk = 35 // Targetkan SDK stabil
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        multiDexEnabled = true // Sudah benar untuk minSdk 24 jika diperlukan

        // Membuat API Key OpenWeather tersedia melalui BuildConfig
        val apiKeyFromProps = localProperties.getProperty("OPEN_WEATHER_API_KEY")
        val apiKeyForBuildConfig = if (apiKeyFromProps.isNullOrBlank()) {
            // Pesan warning ini akan muncul di output Build jika API key tidak ada
            println("WARNING: OPEN_WEATHER_API_KEY not found or empty in local.properties. BuildConfig field will use empty string.")
            "\"\"" // String kosong yang di-escape dengan benar
        } else {
            "\"$apiKeyFromProps\"" // String yang di-escape dengan benar
        }
        buildConfigField("String", "OPEN_WEATHER_API_KEY", apiKeyForBuildConfig)
    }

    buildTypes {
        release {
            isMinifyEnabled = false // Sangat direkomendasikan: true untuk rilis production
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // Anda bisa menambahkan konfigurasi khusus debug di sini jika perlu
            // misalnya: applicationIdSuffix ".debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true // Ini PENTING untuk meng-generate kelas BuildConfig
    }

    androidResources {
        packaging {
            resources {
                noCompress.addAll(listOf(".onnx", ".data", ".model", ".json"))
            }
        }
    }
}

dependencies {
    // Dependensi Inti AndroidX
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.work.runtime.ktx)
    implementation("androidx.compose.material:material-icons-extended")

    // Jetpack Compose - Menggunakan Bill of Materials (BOM)
    val composeBomVersion = "2024.05.00" // Versi stabil umum
    implementation(platform("androidx.compose:compose-bom:$composeBomVersion"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    androidTestImplementation(platform("androidx.compose:compose-bom:$composeBomVersion"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // ViewModel & Lifecycle untuk Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.0")

    // Coroutines Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    // ONNX Runtime
    implementation("com.microsoft.onnxruntime:onnxruntime-android:1.18.0")

    // Jetpack DataStore Preferences
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Navigasi Compose
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Retrofit & Gson Converter
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0") // atau versi terbaru OkHttp 4.x jika Retrofit 2.x
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0") // Opsional

    // Google Play Services Location
    // implementation("com.google.android.gms:play-services-location:21.2.0") // Ganti dengan ini jika tidak pakai libs
    implementation(libs.play.services.location) // Pastikan alias ini benar di libs.versions.toml

    // MultiDex
    implementation("androidx.multidex:multidex:2.0.1")

    // Dependensi Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1") // Cek versi terbaru

    val roomVersion = "2.7.1" // Cek versi terbaru yang stabil
    implementation("androidx.room:room-runtime:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion") // Gunakan ksp, bukan annotationProcessor
    // Opsional: Untuk dukungan Kotlin Coroutines dengan Room
    implementation("androidx.room:room-ktx:$roomVersion")

}