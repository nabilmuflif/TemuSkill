plugins {
    alias(libs.plugins.android.application)
    // TAMBAHKAN PLUGIN INI:
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.temuskill"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.temuskill"
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
}

dependencies {
    // Core AndroidX UI Libraries
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.activity:activity:1.10.0")


    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    // [PENTING] TAMBAHKAN INI UNTUK UPLOAD GAMBAR
    implementation("com.google.firebase:firebase-storage")
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // Retrofit untuk API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp untuk logging
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Glide untuk image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // RecyclerView & CardView
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")

    // ViewPager2
    implementation("androidx.viewpager2:viewpager2:1.0.0")

    // SharedPreferences Encryption
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation ("com.cloudinary:cloudinary-android:2.3.1")
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Dependensi Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
}