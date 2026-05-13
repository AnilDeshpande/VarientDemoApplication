plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.codetutor.varientdemo"
    compileSdk {
        version = release(36)
    }

    flavorDimensions+="env"

    defaultConfig {
        applicationId = "com.codetutor.varientdemo"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        manifestPlaceholders ["appLabel"] = "Varients Demo"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    sourceSets {
        getByName("debug") {
            java.srcDirs("src/debug/java")
        }
        getByName("release") {
            java.srcDirs("src/release/java")
        }
    }

    buildTypes {
        debug {
            //manifestPlaceholders ["appLabel"] = "Varients Demo (dbg)"
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isMinifyEnabled = false

        }
        release {
            //manifestPlaceholders ["appLabel"] = "Varients Demo"
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    productFlavors {

        create("qa"){
            dimension="env"
            applicationIdSuffix=".qa"
            versionNameSuffix="-qa"
            buildConfigField("String","BASE_URL","\"https://qa.api.example.com\"")
        }

        create("staging"){
            dimension="env"
            applicationIdSuffix=".staging"
            versionNameSuffix="-staging"
            buildConfigField("String","BASE_URL","\"https://staging.api.example.com\"")
        }

        create("prod"){
            dimension="env"
            buildConfigField("String", "BASE_URL", "\"https://api.example.com\"")
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
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")

}