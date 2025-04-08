plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin)
    id("kotlin-kapt")
}

android {
    compileSdk = 35
    namespace = "com.faltenreich.skeletonlayout.demo"

    defaultConfig {
        applicationId = "com.faltenreich.skeletonlayout.demo"
        minSdk = 21
        targetSdk = 35
        versionCode = 10
        versionName = "6.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildFeatures.viewBinding = true
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(project(":skeletonlayout"))

    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso)

    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.colorslider)
    
    // Retrofit for network requests
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    
    // Room for database
    implementation("androidx.room:room-runtime:2.7.0-alpha03")
    implementation("androidx.room:room-ktx:2.7.0-alpha03")
    kapt("androidx.room:room-compiler:2.7.0-alpha03")
    
    // WorkManager for background tasks
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    
    // Lifecycle components
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
}