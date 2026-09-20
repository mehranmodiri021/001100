plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.roborazzi)
    alias(libs.plugins.secrets)
}

android {
    namespace = "com.arenaclash.game"

    compileSdk = 36

    defaultConfig {
        applicationId = "com.arenaclash.game"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        val keystorePath = System.getenv("KEYSTORE_PATH")
        val storePasswordEnv = System.getenv("STORE_PASSWORD")
        val keyAliasEnv = System.getenv("KEY_ALIAS")
        val keyPasswordEnv = System.getenv("KEY_PASSWORD")

        if (
            !keystorePath.isNullOrBlank() &&
            !storePasswordEnv.isNullOrBlank() &&
            !keyAliasEnv.isNullOrBlank() &&
            !keyPasswordEnv.isNullOrBlank() &&
            file(keystorePath).exists()
        ) {
            create("release") {
                storeFile = file(keystorePath)
                storePassword = storePasswordEnv
                keyAlias = keyAliasEnv
                keyPassword = keyPasswordEnv
            }
        }
    }

    buildTypes {
        release {
            isCrunchPngs = true
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            signingConfigs.findByName("release")?.let {
                signingConfig = it
            }
        }

        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = true
    }
}

// ═══════════════════════════════════════════════════════════════
// Secrets Plugin: خواندن کلیدها از local.properties
// ═══════════════════════════════════════════════════════════════
// نکته: به جای .env، از local.properties استفاده می‌کنیم چون
// پلاگین secrets با این فایل بهتر کار می‌کند و در CI/CD هم
// به راحتی از روی GitHub Secrets ساخته می‌شود.
secrets {
    propertiesFileName = "local.properties"
    defaultPropertiesFileName = "local.properties"
}

dependencies {
    // ─── Compose ───
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)

    // ─── Core ───
    implementation(libs.androidx.core.ktx)

    // ─── Lifecycle ───
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // ─── Navigation ───
    implementation(libs.androidx.navigation.compose)

    // ─── Room ───
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)

    // ─── Moshi ───
    implementation(libs.moshi.kotlin)

    // ─── تپسل پلاس SDK (تبلیغات) ───
    implementation("ir.tapsell.plus:tapsell-plus-sdk-android:2.3.3")

    // ─── تست‌ها ───
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.androidx.core)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.junit.rule)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.runner)

    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // ─── KSP برای Room و Moshi ───
    ksp(libs.androidx.room.compiler)
    ksp(libs.moshi.kotlin.codegen)
}
