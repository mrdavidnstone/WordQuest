import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "SpellArcade"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
                implementation("org.jetbrains.compose.material:material-icons-extended:1.7.0")
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("androidx.core:core-ktx:1.15.0")
                implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
                implementation("androidx.activity:activity-compose:1.9.3")
                implementation("androidx.compose.ui:ui-tooling-preview")
                implementation("androidx.compose.material3:material3")
                implementation("androidx.navigation:navigation-compose:2.8.4")
                implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
                implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
                implementation("androidx.room:room-runtime:2.6.1")
                implementation("androidx.room:room-ktx:2.6.1")
                implementation("androidx.datastore:datastore-preferences:1.1.1")
                implementation("com.google.dagger:hilt-android:2.52")
                implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
            }
        }
        val iosMain by creating {
            dependsOn(commonMain)
        }
    }
}

android {
    namespace = "com.spellarcade.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.spellarcade.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
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
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    ksp("com.google.dagger:hilt-compiler:2.52")
    ksp("androidx.room:room-compiler:2.6.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
