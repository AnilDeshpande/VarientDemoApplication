plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.codetutor.varientdemo"
    compileSdk = 36

    flavorDimensions+=listOf("env","tier")

    defaultConfig {
        applicationId = "com.codetutor.varientdemo"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    sourceSets {
        getByName("debug"){
            java.srcDirs("src/debug/java")
        }
        getByName("release"){
            java.srcDirs("src/release/java")
        }
    }

    buildTypes {

        debug {
            //manifestPlaceholders ["appLabel"] = "Varient Demo (dbg)"
            applicationIdSuffix=".debug"
            versionNameSuffix="-debug"
            isMinifyEnabled = false
        }

        release {
            //manifestPlaceholders ["appLabel"] = "Varient Demo"
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
            buildConfigField("String","BASE_URL","\"https://api.example.com\"")
        }

        create("free"){
            dimension="tier"
            applicationIdSuffix=".free"
            versionNameSuffix="-free"
            buildConfigField("Boolean","IS_PAID","false")
        }

        create("paid"){
            dimension="tier"
            applicationIdSuffix=".paid"
            versionNameSuffix="-paid"
            buildConfigField("Boolean","IS_PAID","true")
        }

        getByName("free"){
            buildConfigField("String","TIER_NAME","\"free\"")
            buildConfigField("Boolean","SHOW_ADS","true")
        }

        getByName("paid"){
            buildConfigField("String","TIER_NAME","\"paid\"")
            buildConfigField("Boolean","SHOW_ADS","false")
        }
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
    "freeImplementation"(project(":ads"))
}