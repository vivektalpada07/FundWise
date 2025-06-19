plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.fundwiseapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.fundwiseapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // ✅ Enable ViewBinding
    buildFeatures {
        viewBinding = true
    }
}


dependencies {
    // ✅ Core AndroidX
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // ✅ Firebase
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.database)
    implementation(libs.mpandroidchart)

    // ✅ Bottom Navigation + Fragment support
    implementation("androidx.fragment:fragment:1.6.2")
    implementation("androidx.navigation:navigation-fragment:2.7.7")
    implementation("androidx.navigation:navigation-ui:2.7.7")

    // ✅ ViewModel + LiveData (optional but useful)
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.7.0")

    // ✅ UI Component (already present but kept latest version)
    implementation("com.google.android.material:material:1.11.0")
// firebase auth
    implementation("com.google.firebase:firebase-auth:23.2.1")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
// Retrofit & OkHttp
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.12.0")
    //implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
// Retrofit Converter for plain text responses
    implementation ("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.3")
    implementation ("androidx.room:room-runtime:2.6.1")
    annotationProcessor ("androidx.room:room-compiler:2.6.1")


    // ✅ Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
