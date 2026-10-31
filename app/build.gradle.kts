plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "app.onedoc"
    compileSdk = 36

    defaultConfig {
        applicationId = "app.onedoc"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // BuildConfig provenance (populated by build script / CI)
        buildConfigField("String", "PROVENANCE_SHA", "\"dev-local\"")
        buildConfigField("long", "BUILD_TIME", "0")
    }

    signingConfigs {
        named("debug") { /* debug keystore provided by AGP */ }
    }

    buildTypes {
        named("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // Release signing: owner keystore supplied at G8 (RC keystore until then).
        }
    }

    flavorDimensions += "tier"
    productFlavors {
        create("full") {
            dimension = "tier"
            applicationId = "app.onedoc"
        }
        create("writer") {
            dimension = "tier"
            applicationId = "app.onedoc"
        }
    }

    sourceSets {
        val fullMain by getting
        fullMain.java.srcDirs("src/full/java")
        val writerMain by getting
        writerMain.java.srcDirs("src/writer/java")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.2")
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.ui)
    implementation(libs.compose.graphics)
    implementation(libs.compose.material)
    implementation(libs.compose.tooling)
    debugImplementation(libs.compose.tooling.preview)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.datastore.preferences)
    implementation(libs.koin.core)
    implementation(libs.koin.android)

    // JSON for document serialization (§14.2, Appendix AN)
    implementation("org.json:json:20231013")

    // Coroutines
    implementation(libs.androidx.core.ktx) // already above
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    testImplementation(libs.junit)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.robolectric)
}
