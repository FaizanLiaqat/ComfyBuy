plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    // Add the Kotlin Kapt plugin here
    kotlin("kapt")
    // If you are using performance monitoring or Crashlytics, keep these:
    // alias(libs.plugins.firebase.perf)
    // alias(libs.plugins.firebase.crashlytics)
}

android {
    namespace = "com.muhammadahmedmufii.comfybuy"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.muhammadahmedmufii.comfybuy"
        minSdk = 24
        targetSdk = 35
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


    implementation("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.8.0")

    // Keep your existing dependencies
    implementation ("com.github.bumptech.glide:glide:4.16.0") // Note: You have two Glide versions, keep one.
    implementation ("com.google.android.material:material:1.8.0") // Note: You have this and libs.material, keep one.
    implementation ("de.hdodenhof:circleimageview:3.1.0") // Note: You have this twice, keep one.
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material) // Use this one for Material Design components
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database) // Keep if you plan to use Realtime Database for anything else
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.firestore)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Libraries (clean up duplicates - keeping one of each)
    // implementation ("de.hdodenhof:circleimageview:3.1.0") // Duplicate
    // implementation ("com.github.bumptech.glide:glide:4.15.1") // Duplicate - keep 4.16.0
    // annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1") // Duplicate - use the version matching your Glide implementation

    // Add the correct annotationProcessor for Glide 4.16.0
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")


    implementation("com.google.android.gms:play-services-auth:20.7.0") // Use the latest version if needed

    // Room Components - Add these lines
    val room_version = "2.6.1" // Use the latest stable version

    implementation("androidx.room:room-runtime:$room_version")
    // Use kapt for Kotlin annotation processing
    kapt("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version") // Kotlin Extensions for Room

    // Optional: Room Testing
    testImplementation("androidx.room:room-testing:$room_version")

    // Add WorkManager dependency for synchronization
    val work_version = "2.9.0" // Use the latest stable version
    implementation("androidx.work:work-runtime-ktx:$work_version") // Kotlin + coroutines
    // Optional: WorkManager with Room

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3") // Use the latest version
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3") // Use the latest version

    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0") // Use the latest version
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    implementation("com.google.code.gson:gson:2.10.1")

}
