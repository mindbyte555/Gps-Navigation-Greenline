plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.example.gpstest"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.voicenavigation.gpslocation.routefinder.mapdirection"
        minSdk = 26
        targetSdk = 35

        versionCode = 41
        versionName = "5.1"
        multiDexEnabled = true
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
    bundle {
        language {
            enableSplit = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        //   isCoreLibraryDesugaringEnabled = true
//        sourceCompatibility = JavaVersion.VERSION_1_8
//        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "17"
//        jvmTarget = "1.8"
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

}
dependencies {

//  coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")
    implementation("com.mapbox.maps:android:10.16.0")
    implementation("com.mapbox.navigation:android:2.16.0")
    implementation("com.mapbox.search:mapbox-search-android-ui:1.0.0-beta.43")
    implementation("com.google.code.gson:gson:2.8.8")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.androidx.lifecycle.process)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.material.v140) //VIEWPAGER
    implementation(libs.androidx.viewpager)
    implementation(libs.sdp.android)
    //dot indicator
    implementation(libs.dotsindicator)
    implementation(libs.androidx.viewpager2)
    implementation(libs.lottie)
    implementation(libs.androidx.multidex)
    implementation(libs.github.glide)
    annotationProcessor(libs.compiler)
    //firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.config)
    implementation("com.github.amitshekhariitbhu.Fast-Android-Networking:android-networking:1.0.4")
    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // ads
    implementation("com.google.android.gms:play-services-ads:24.3.0")
    implementation("com.google.android.ump:user-messaging-platform:2.2.0")
    //shimmer
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    //billig
    implementation("com.android.billingclient:billing-ktx:7.0.0")
    implementation("com.google.firebase:firebase-messaging:25.0.0")

    implementation(libs.facebook)
    //implementation("com.google.ads.mediation:mintegral:16.8.61.0")
    //implementation ("com.microsoft.clarity:clarity:3.2.1")

}
