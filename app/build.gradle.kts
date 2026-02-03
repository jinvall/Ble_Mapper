// /data/data/com.termux/files/home/Blu/BleMapper/app/build.gradle.kts

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.jamjam.blemapper"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.jamjam.blemapper"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    packaging {
        resources.excludes.add("/META-INF/{AL2.0,LGPL2.1}")
    }
}

dependencies {
    // Compose BOM for version alignment
    val composeBom = platform("androidx.compose:compose-bom:2024.01.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Compose core
	implementation("androidx.compose.ui:ui")
	implementation("androidx.compose.ui:ui-tooling-preview")

	// Material3 core
	implementation("androidx.compose.material3:material3:1.2.1")
	implementation("androidx.compose.material3:material3-window-size-class:1.2.1")

	// Material3 adaptive (REAL versions)
	implementation("androidx.compose.material3:material3-adaptive:1.0.0-alpha04")
	implementation("androidx.compose.material3:material3-adaptive-navigation-suite:1.0.0-alpha04")

	// Material3 theme adapter (LAST published version)
	implementation("androidx.compose.material3:material3-theme-adapter:1.1.2")



    // Activity + Compose integration
    implementation("androidx.activity:activity-compose:1.8.2")

    // Lifecycle (for state handling)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    // BLE scanning uses Android framework APIs — no extra libs needed
    implementation("androidx.core:core-ktx:1.12.0")

    // Debug tools
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

