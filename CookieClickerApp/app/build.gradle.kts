plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.cookieclicker"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.cookieclicker"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        val sdkBaseUrl = (project.findProperty("SDK_BASE_URL") as String?)?.trim()?.ifEmpty { null }
            /* Default to the deployed API (override with SDK_BASE_URL in gradle.properties for local dev) */
            ?: "https://ad-provider-web-part.onrender.com"
        val sdkAppKey = (project.findProperty("SDK_APP_KEY") as String?)?.trim()?.ifEmpty { null }
            ?: "demo-app"

        buildConfigField("String", "SDK_BASE_URL", "\"$sdkBaseUrl\"")
        buildConfigField("String", "SDK_APP_KEY", "\"$sdkAppKey\"")
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":engagementsdk"))

    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.fragment:fragment-ktx:1.8.8")
}
