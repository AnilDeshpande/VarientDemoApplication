import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

fun git(vararg args: String): String? = try {
    val process = ProcessBuilder("git", *args)
        .directory(rootProject.projectDir)
        .redirectErrorStream(true)
        .start()
    val output = process.inputStream.bufferedReader().use { it.readText().trim() }
    if (process.waitFor() == 0) output.takeIf { it.isNotBlank() } else null
} catch (_: Exception) {
    null
}

fun semVerToVersionCode(version: String): Int? {
    val match = Regex("""(\d+)\.(\d+)\.(\d+)""").matchEntire(version) ?: return null
    val (major, minor, patch) = match.destructured
    return major.toInt() * 10000 + minor.toInt() * 100 + patch.toInt()
}

val versionProps = Properties().apply {
    load(rootProject.file("version.properties").inputStream())
}

val major = versionProps["MAJOR"].toString().toInt()
val minor = versionProps["MINOR"].toString().toInt()
val patch = versionProps["PATCH"].toString().toInt()

val fallbackVersionName = "$major.$minor.$patch"
val fallbackVersionCode = major * 10000 + minor * 100 + patch

val latestTag = git("describe", "--tags", "--abbrev=0")
val sha = git("rev-parse", "--short", "HEAD") ?: "nogit"
val commitsSinceTag = latestTag?.let { tag ->
    git("rev-list", "$tag..HEAD", "--count")?.toIntOrNull() ?: 0
} ?: 0

val baseTagVersion = latestTag?.removePrefix("v")
val baseVersionName = baseTagVersion ?: fallbackVersionName
val baseVersionCode = baseTagVersion?.let(::semVerToVersionCode) ?: fallbackVersionCode

// Tagged releases stay clean. Builds ahead of a tag carry trace metadata.
val versionNameFromGit = when {
    latestTag == null -> "$fallbackVersionName+local.$sha"
    commitsSinceTag == 0 -> baseVersionName
    else -> "$baseVersionName+$commitsSinceTag.$sha"
}

val versionCodeFromGit = baseVersionCode + commitsSinceTag


android {
    namespace = "com.codetutor.varientdemo"
    compileSdk = 36

    flavorDimensions+=listOf("env","tier")

    defaultConfig {
        applicationId = "com.codetutor.varientdemo"
        minSdk = 24
        targetSdk = 36
        versionCode = versionCodeFromGit
        versionName = versionNameFromGit

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

/*
extensions.configure<AndroidComponentsExtension<*, *, *>>("androidComponents") {
    beforeVariants { variantBuilder ->
        val flavors = variantBuilder.productFlavors.toMap()
        val env = flavors["env"]
        val tier = flavors["tier"]
        val buildType = variantBuilder.buildType

        //Fine the combinations to keep

        val allowedVariants = setOf(
            //QA - Only Debug builds
            Triple("qa", "free", "debug"),
            Triple("qa", "paid", "debug"),

            //Staging - both Debug and Release
            Triple("staging", "free", "debug"),
            Triple("staging", "paid", "debug"),
            //Triple("staging", "free", "release"),
            //Triple("staging", "paid", "release"),

            //Prod - Only release builds
            Triple("prod", "free", "release"),
            Triple("prod", "paid", "release")
        )

        //Disable variants if not allowed in the allowedVariants list
        val currentVariant = Triple(env, tier,buildType)
        if(currentVariant !in allowedVariants) {
            variantBuilder.enable = false
        }

    }
}*/
