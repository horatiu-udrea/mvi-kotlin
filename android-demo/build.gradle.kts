plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.example.android_demo"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    }
}

dependencies {
    implementation(project(":mvi"))
    implementation(libs.kotlinx.coroutines)
    implementation(libs.material)
    implementation(libs.viewmodel)
    implementation(platform(libs.composeBom))
    implementation(libs.material)
    implementation(libs.compose.tooling.preview)
    debugImplementation(libs.compose.tooling)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit)
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.kotlinx.coroutines.test)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}