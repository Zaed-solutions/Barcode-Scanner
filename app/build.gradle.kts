plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.realm)
//    alias(libs.plugins.firebase)
}

android {
    namespace = "com.zaed.barcodescanner"
    compileSdk = 34
    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/ASL2.0"
            )
        }
    }
    defaultConfig {
        applicationId = "com.zaed.barcodescanner"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.kotlin.compose.compiler.plugin)

    //Kotlinx-Serialization
    implementation(libs.kotlinx.serialization.json)
    //KTor
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.core)
    //Kotlinx-DateTime
    implementation(libs.kotlinx.datetime)
    //Compose ViewModel Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    //Compose Navigation
    implementation(libs.androidx.navigation.compose)
    //Material3 Extended Icons
    implementation(libs.androidx.material.icons.extended)
    //Realm
//    implementation (libs.realm.base)
    //Realm - If using Device Sync
//    implementation (libs.realm.sync)
    //Kotlinx-Coroutines
    implementation (libs.kotlinx.coroutines.core)
    //Coil
    implementation(libs.coil.compose)
    //Splash Screen
    implementation(libs.androidx.core.splashscreen)
    //Koin
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.androidx.compose.navigation)
    //Firebase
//    implementation(platform(libs.firebase.bom))
//    implementation(libs.firebase.auth.ktx)
//    implementation(libs.firebase.firestore.ktx)
//    implementation(libs.firebase.storage.ktx)
    //Google Fonts
    implementation(libs.androidx.ui.text.google.fonts)
    //Lottie
    implementation(libs.lottie.compose)
    implementation("com.google.api-client:google-api-client-android:1.33.0")
    implementation ("com.google.api-client:google-api-client:2.0.0")
    implementation ("com.google.oauth-client:google-oauth-client-jetty:1.34.1")
    implementation ("com.google.apis:google-api-services-drive:v3-rev20220815-2.0.0")
    implementation ("com.google.android.gms:play-services-auth:20.7.0")

    //google code scanner
    implementation(libs.play.services.code.scanner)
    implementation("androidx.work:work-runtime-ktx:2.8.1")

}