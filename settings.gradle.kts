pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // مخزن اختصاصی تپسل برای دانلود SDK تپسل پلاس
        maven { url = uri("https://repo.tapsell.ir/repository/tapsell-android/") }
        // مخزن JitPack برای Poolakey (IAP کافه‌بازار)
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "Arena Clash"

include(":app")
