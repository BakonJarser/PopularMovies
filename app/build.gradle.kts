plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("com.google.devtools.ksp")
    id("androidx.navigation.safeargs.kotlin")
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
        dataBinding = true
        buildConfig = true
    }

    namespace = "com.cellblock70.popularmovies"
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1") {
        exclude(group = "com.android.support", module = "support-annotations")
    }
    testImplementation("junit:junit:4.13.2")

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.vectordrawable:vectordrawable:1.2.0")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    api("com.google.android.material:material:1.12.0")
    implementation("androidx.preference:preference-ktx:1.2.1")

    // Room database
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("com.squareup.moshi:moshi:1.15.2")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.2")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.14")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.9")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.9")
    implementation("com.jakewharton.timber:timber:5.0.1")
}
