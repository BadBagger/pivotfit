import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

val hasLocalReleaseSigning =
    rootProject.file("release-keystore.jks").exists() &&
        localProperties.getProperty("pivotfit.storePassword") != null &&
        localProperties.getProperty("pivotfit.keyAlias") != null &&
        localProperties.getProperty("pivotfit.keyPassword") != null

android {
    namespace = "com.pivotfit.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.pivotfit.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 8
        versionName = "0.1.7-rest-timer-ui"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (hasLocalReleaseSigning) {
            create("localRelease") {
                storeFile = rootProject.file("release-keystore.jks")
                storePassword = localProperties.getProperty("pivotfit.storePassword")
                keyAlias = localProperties.getProperty("pivotfit.keyAlias")
                keyPassword = localProperties.getProperty("pivotfit.keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (hasLocalReleaseSigning) {
                signingConfig = signingConfigs.getByName("localRelease")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.datastore.preferences)

    ksp(libs.androidx.room.compiler)

    testImplementation(libs.junit)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
