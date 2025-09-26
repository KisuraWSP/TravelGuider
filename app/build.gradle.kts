plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("com.google.devtools.ksp") version "2.0.21-1.0.25"
}

android {
    namespace = "com.app.tourism_app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.app.tourism_app"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.0.21")

    // AndroidX
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.recyclerview:recyclerview:1.3.1")

    // Lifecycle / ViewModel
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")

    // Room
    implementation("androidx.room:room-runtime:2.7.0-alpha06")
    implementation(libs.androidx.activity)
    implementation(libs.androidx.preference)
    ksp("androidx.room:room-compiler:2.7.0-alpha06")
    implementation("androidx.room:room-ktx:2.7.0-alpha06")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")

    implementation("com.google.android.gms:play-services-maps:19.2.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation("androidx.preference:preference-ktx:1.2.1")

    // Kotlin coroutines, Retrofit, Moshi (or Gson), Room, lifecycle
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.moshi:moshi:1.15.2")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Moshi + Kotlin adapter + optional codegen
    implementation("com.squareup.moshi:moshi-kotlin:1.15.2")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.15.2")

    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")

    implementation("com.google.android.material:material:1.12.0")
}