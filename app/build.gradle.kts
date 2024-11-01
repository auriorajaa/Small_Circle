plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
}

android {
    namespace = "com.org.smallcircle"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.org.smallcircle"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Firebase Library
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.crashlytics)
    implementation("com.google.android.gms:play-services-auth:21.2.0") // Login with google library

    // Code Picker Library
    implementation("com.hbb20:ccp:2.7.0")

    // Glide
    implementation ("com.github.bumptech.glide:glide:4.16.0")

    // Makeramen
    implementation("com.makeramen:roundedimageview:2.3.0")

    // Material Design
    implementation("com.google.android.material:material:1.11.0")

    // Lottie Animation
    implementation("com.airbnb.android:lottie:6.3.0")

    // Shimmer Effect
    implementation("com.facebook.shimmer:shimmer:0.5.0")

    implementation ("androidx.browser:browser:1.5.0")
    implementation ("com.google.android.play:integrity:1.1.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}