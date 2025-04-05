plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
    kotlin("plugin.serialization") version "2.0.21"
}

android {

    compileSdk = 35
    defaultConfig {
        applicationId = "com.cellblock70.popularmovies"
        minSdk = 21
        targetSdk = 35
        versionCode = 2
        versionName = "2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }

    buildTypes.all {
        // This assumes TmdbApiKey is defined elsewhere in your gradle.properties or as an environment variable
        buildConfigField("String", "TMDB_MAP_API_KEY", "\"${findProperty("TmdbApiKey") ?: ""}\"")
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    namespace = "com.cellblock70.popularmovies"
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1") {
        exclude(group = "com.android.support", module = "support-annotations")
    }
    testImplementation("junit:junit:4.13.2")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    debugImplementation("androidx.compose.ui:ui-tooling:1.7.8")

    // Room database
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("com.squareup.moshi:moshi:1.15.2")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.2")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.14")
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Compose
    implementation("androidx.compose.ui:ui:1.7.8")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.compose.material3:material3:1.3.1")
    implementation("androidx.compose.ui:ui-graphics:1.7.8")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.8")
    implementation("androidx.compose.foundation:foundation:1.7.8")
    implementation("androidx.compose.runtime:runtime:1.7.8")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.navigation:navigation-compose:2.8.9")
    implementation("io.coil-kt.coil3:coil-compose:3.1.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.1.0")
    implementation("androidx.compose.compiler:compiler:1.5.15")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
}
