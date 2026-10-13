plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.musify.browser"
    compileSdk = 35

    val targetAbi = project.findProperty("TARGET_ABI") as String?

    defaultConfig {
        applicationId = "com.musify.browser"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.9.0-beta"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        ndk {
            abiFilters.clear()
            if (targetAbi != null && targetAbi.isNotBlank()) {
                abiFilters.add(targetAbi)
            } else {
                abiFilters.addAll(listOf("arm64-v8a", "x86_64"))
            }
        }
    }

    signingConfigs {
        create("release") {
            val keyStorePath = project.findProperty("RELEASE_STORE_FILE") as String? ?: "musify.keystore"
            val keyStoreObj = file(keyStorePath)
            if (keyStoreObj.exists()) {
                storeFile = keyStoreObj
                storePassword = project.findProperty("RELEASE_STORE_PASSWORD") as String? ?: "musify123"
                keyAlias = project.findProperty("RELEASE_KEY_ALIAS") as String? ?: "musify"
                keyPassword = project.findProperty("RELEASE_KEY_PASSWORD") as String? ?: "musify123"
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
                enableV4Signing = false
            }
        }
        getByName("debug") {
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = false
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val releaseConfig = signingConfigs.getByName("release")
            if (releaseConfig.storeFile != null && releaseConfig.storeFile!!.exists()) {
                signingConfig = releaseConfig
            } else {
                signingConfig = signingConfigs.getByName("debug")
            }
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
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

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            if (project.hasProperty("BUILD_LITE")) {
                excludes += listOf("lib/**", "assets/omni.ja")
            }
        }
        jniLibs {
            useLegacyPackaging = true
            if (project.hasProperty("BUILD_LITE")) {
                excludes += listOf("**/*")
            } else if (targetAbi != null && targetAbi.isNotBlank()) {
                val allAbis = listOf("x86", "x86_64", "armeabi-v7a", "arm64-v8a")
                val otherAbis = allAbis.filter { it != targetAbi }
                otherAbis.forEach { abi ->
                    excludes += listOf("lib/$abi/**", "**/$abi/**")
                }
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.webkit)
    implementation(libs.geckoview.omni)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
