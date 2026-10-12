plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {

    // Define semantic versioning components
    // TODO: MAINTAIN/UPDATE THESE
    val versionMajor = 1
    val versionMinor = 1
    val versionPatch = 0
    val versionBuild = 0 // Incremented via CI/CD if needed

    namespace = "com.example.gnssreceiver"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.gnssreceiver"
        minSdk = 26
        targetSdk = 37


//        versionCode = 1
//        versionName = "1.0"

        // E.g. "1.2.3"
        versionName = "$versionMajor.$versionMinor.$versionPatch"

        // E.g.: 1020300
        versionCode = (versionMajor * 1000000) + (versionMinor * 10000) + (versionPatch * 100) + versionBuild


        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.compose.icons)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.play.location)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}