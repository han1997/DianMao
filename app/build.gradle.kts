import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

val appVersionCode = (findProperty("app.versionCode") as? String)?.toIntOrNull() ?: 1
val appVersionName = findProperty("app.versionName") as? String ?: "1.0.0"

// Release signing: credentials live in the git-ignored keystore.properties so
// other environments (CI, teammates) simply skip signing instead of failing.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val releaseSigningProps = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

android {
    namespace = "fun.han1997.dianmao"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "fun.han1997.dianmao"
        minSdk = 31
        targetSdk = 36
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                storeFile = rootProject.file(releaseSigningProps.getProperty("storeFile"))
                storePassword = releaseSigningProps.getProperty("storePassword")
                keyAlias = releaseSigningProps.getProperty("keyAlias")
                keyPassword = releaseSigningProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // Debug builds keep the SDK debug signing config.
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
    }

    // Output APKs as dianmao-<versionName>.apk for both build types.
    applicationVariants.all {
        val variantName = name
        outputs.all {
            if (this is com.android.build.gradle.internal.api.ApkVariantOutputImpl) {
                this.outputFileName = "dianmao-${defaultConfig.versionName}${if (variantName == "release") "" else "-$variantName"}.apk"
            }
        }
    }
}

/**
 * Release version bump: `versionCode + 1`, optionally advancing
 * `versionName` via -PversionNameBump=patch|minor|major, then writing both
 * back to the root gradle.properties. Run before assembleRelease so every
 * shipped build carries a fresh versionCode.
 */
tasks.register("bumpVersion") {
    group = "versioning"
    description = "Increments app.versionCode (and optionally app.versionName) in gradle.properties"
    doLast {
        val propsFile = rootProject.file("gradle.properties")
        val lines = propsFile.readLines().toMutableList()

        fun setProperty(key: String, newValue: String) {
            val idx = lines.indexOfFirst { it.trimStart().startsWith("$key=") }
            if (idx >= 0) lines[idx] = "$key=$newValue" else lines.add("$key=$newValue")
        }

        val currentCode = (lines.firstOrNull { it.trimStart().startsWith("app.versionCode=") }
            ?.substringAfter('=')?.trim()?.toIntOrNull()) ?: 1
        val newCode = currentCode + 1
        setProperty("app.versionCode", newCode.toString())

        val bump = providers.gradleProperty("versionNameBump").orNull
        if (bump != "none") {
            val currentName = (lines.firstOrNull { it.trimStart().startsWith("app.versionName=") }
                ?.substringAfter('=')?.trim()) ?: "1.0.0"
            val parts = currentName.split('.').map { it.toIntOrNull() ?: 0 }
            val padded = (parts + listOf(0, 0, 0)).take(3).toMutableList()
            when (bump) {
                null, "patch" -> padded[2] += 1
                "minor" -> { padded[1] += 1; padded[2] = 0 }
                "major" -> { padded[0] += 1; padded[1] = 0; padded[2] = 0 }
                else -> throw GradleException("versionNameBump must be patch|minor|major")
            }
            setProperty("app.versionName", padded.joinToString("."))
        }

        propsFile.writeText(lines.joinToString(System.lineSeparator()) + System.lineSeparator())
        logger.lifecycle("bumped to versionCode=$newCode")
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
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
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.glance.appwidget)
    debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
