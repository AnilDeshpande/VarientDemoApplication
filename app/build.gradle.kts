import com.android.build.api.variant.AndroidComponentsExtension
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

// ── Secret resolution ──────────────────────────────────────────────────────
// Two-tier lookup: env var (CI) → local.properties (local dev) → fail-fast error.
// Blank env vars (e.g. '') are treated as absent — CI intentionally sets
// release-only vars to '' for debug builds.
val localProps = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(file.inputStream())
}

fun resolveSecretOrNull(name: String): String? =
    System.getenv(name)?.takeIf { it.isNotBlank() }
        ?: localProps.getProperty(name)?.takeIf { it.isNotBlank() }

fun resolveSecret(name: String): String =
    resolveSecretOrNull(name)
        ?: error("Missing secret: $name — add it to local.properties or set it as an env var.")

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

        // ── COMMON secret — same for every variant ─────────────────
        buildConfigField("String", "ANALYTICS_SDK_KEY", "\"${resolveSecret("ANALYTICS_SDK_KEY")}\"")
    }

    sourceSets {
        getByName("debug"){
            java.srcDirs("src/debug/java")
        }
        getByName("release"){
            java.srcDirs("src/release/java")
        }
    }

    // ── RELEASE-ONLY signing config ───────────────────────────────────────
    // This block runs at Gradle CONFIGURATION time for ALL variants.
    // For debug builds (locally or on CI) the signing secrets may be absent,
    // so we use resolveSecretOrNull and skip configuration when missing.
    // For release builds the secrets MUST be present — the release buildType
    // references this config, and Gradle will fail at signing time if empty.
    signingConfigs {
        create("release") {
            val storeFilePath = resolveSecretOrNull("RELEASE_SIGNING_STORE_FILE")
            if (storeFilePath != null) {
                storeFile = file(storeFilePath)
                storePassword = resolveSecretOrNull("RELEASE_SIGNING_STORE_PASSWORD") ?: ""
                keyAlias = resolveSecretOrNull("RELEASE_SIGNING_KEY_ALIAS") ?: ""
                keyPassword = resolveSecretOrNull("RELEASE_SIGNING_KEY_PASSWORD") ?: ""
            }
        }
    }

    buildTypes {

        debug {
            //manifestPlaceholders ["appLabel"] = "Varient Demo (dbg)"
            applicationIdSuffix=".debug"
            versionNameSuffix="-debug"
            isMinifyEnabled = false
            // Debug uses the auto-generated debug keystore — no secrets needed
        }

        release {
            //manifestPlaceholders ["appLabel"] = "Varient Demo"
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // ❌ THE NAÏVE APPROACH — DO NOT DO THIS
    // If you create one secret per variant you end up with:
    //
    //   BACKEND_TOKEN_QA_FREE_DEBUG    = "..."
    //   BACKEND_TOKEN_QA_FREE_RELEASE  = "..."
    //   BACKEND_TOKEN_QA_PAID_DEBUG    = "..."
    //   BACKEND_TOKEN_QA_PAID_RELEASE  = "..."
    //   BACKEND_TOKEN_STAGING_FREE_DEBUG   = "..."
    //   BACKEND_TOKEN_STAGING_FREE_RELEASE = "..."
    //   BACKEND_TOKEN_STAGING_PAID_DEBUG   = "..."
    //   BACKEND_TOKEN_STAGING_PAID_RELEASE = "..."
    //   BACKEND_TOKEN_PROD_FREE_DEBUG  = "..."
    //   BACKEND_TOKEN_PROD_FREE_RELEASE= "..."
    //   BACKEND_TOKEN_PROD_PAID_DEBUG  = "..."
    //   BACKEND_TOKEN_PROD_PAID_RELEASE= "..."
    //
    // That's 12 secrets — just for ONE key. Now multiply by every secret
    // your app needs (ad SDK key, analytics key, signing creds…).
    // The workflow YAML becomes a giant if-else matrix. Copy-paste Gradle logic
    // everywhere. It collapses fast — not because it's technically wrong,
    // but because no one on the team can maintain it six months later.
    //
    // The real question: what ACTUALLY needs to vary, and what can stay common?
    // ────────────────────────────────────────────────────────────────────────

    productFlavors {
        create("qa"){
            dimension="env"
            applicationIdSuffix=".qa"
            versionNameSuffix="-qa"
            buildConfigField("String","BASE_URL","\"https://qa.api.example.com\"")
            // ── ENVIRONMENT-SPECIFIC secret ────────────────────────
            buildConfigField("String", "BACKEND_TOKEN", "\"${resolveSecret("BACKEND_TOKEN_QA")}\"")
        }

        create("staging"){
            dimension="env"
            applicationIdSuffix=".staging"
            versionNameSuffix="-staging"
            buildConfigField("String","BASE_URL","\"https://staging.api.example.com\"")
            // ── ENVIRONMENT-SPECIFIC secret ────────────────────────
            buildConfigField("String", "BACKEND_TOKEN", "\"${resolveSecret("BACKEND_TOKEN_STAGING")}\"")
        }

        create("prod"){
            dimension="env"
            buildConfigField("String","BASE_URL","\"https://api.example.com\"")
            // ── ENVIRONMENT-SPECIFIC secret ────────────────────────
            buildConfigField("String", "BACKEND_TOKEN", "\"${resolveSecret("BACKEND_TOKEN_PROD")}\"")
        }

        create("free"){
            dimension="tier"
            applicationIdSuffix=".free"
            versionNameSuffix="-free"
            buildConfigField("Boolean","IS_PAID","false")
            // ── TIER-SPECIFIC secret ───────────────────────────────
            buildConfigField("String", "AD_SDK_KEY", "\"${resolveSecret("AD_SDK_KEY_FREE")}\"")
        }

        create("paid"){
            dimension="tier"
            applicationIdSuffix=".paid"
            versionNameSuffix="-paid"
            buildConfigField("Boolean","IS_PAID","true")
            // ── TIER-SPECIFIC secret ───────────────────────────────
            buildConfigField("String", "AD_SDK_KEY", "\"${resolveSecret("AD_SDK_KEY_PAID")}\"")
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

androidComponents {
    onVariants { variant ->
        if(variant.buildType == "release") {
            variant.outputs.forEach { output ->
                output.versionName.set(baseVersionName)
            }
        }
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

// ── Variant filter ─────────────────────────────────────────────────────
// Only build the combinations that make sense for our workflow:
//   QA        → debug only   (internal testing, no release signing needed)
//   Staging   → debug only   (integration testing)
//   Prod      → release only (what ships to users)
// This reduces 12 variants to 6.
extensions.configure<AndroidComponentsExtension<*, *, *>>("androidComponents") {
    beforeVariants { variantBuilder ->
        val flavors = variantBuilder.productFlavors.toMap()
        val env = flavors["env"]
        val tier = flavors["tier"]
        val buildType = variantBuilder.buildType

        val allowedVariants = setOf(
            // QA — only debug builds
            Triple("qa", "free", "debug"),
            Triple("qa", "paid", "debug"),

            // Staging — only debug builds
            Triple("staging", "free", "debug"),
            Triple("staging", "paid", "debug"),

            // Prod — only release builds
            Triple("prod", "free", "release"),
            Triple("prod", "paid", "release")
        )

        val currentVariant = Triple(env, tier, buildType)
        if (currentVariant !in allowedVariants) {
            variantBuilder.enable = false
        }
    }
}
