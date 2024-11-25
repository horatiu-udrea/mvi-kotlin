import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("module.publication")
}

kotlin {
    explicitApi()
    jvm()
    androidTarget {
        publishLibraryVariants("release")
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }
   // iosX64()
   // iosArm64()
   // iosSimulatorArm64()
   // linuxX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.junit)
                implementation(libs.kotest.assertions)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}

android {
    namespace = "ro.horatiu_udrea.mvi"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}