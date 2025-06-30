plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.0"
    kotlin("plugin.serialization") version "2.0.21"
    id("dagger.hilt.android.plugin")
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

    kotlin.compilerOptions.freeCompilerArgs.add("-Xannotation-default-target=param-property")

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

    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.core:core-ktx:1.16.0")

    // Room database
    val roomVersion = "2.7.2"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    implementation("com.squareup.moshi:moshi:1.15.2")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.2")
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-moshi:3.0.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.16")
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Compose
    val composeVersion = "1.8.3"
    debugImplementation("androidx.compose.ui:ui-tooling:$composeVersion")
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.ui:ui-graphics:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
    implementation("androidx.compose.foundation:foundation:$composeVersion")
    implementation("androidx.compose.runtime:runtime:$composeVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.1")
    implementation("androidx.compose.material3:material3:1.3.2")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.navigation:navigation-compose:2.9.0")
    implementation("io.coil-kt.coil3:coil-compose:3.2.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.2.0")
    implementation("androidx.compose.compiler:compiler:1.5.15")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    // Hilt
    val hiltVersion = "2.56.2"
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    ksp("com.google.dagger:hilt-android-compiler:$hiltVersion")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
}
