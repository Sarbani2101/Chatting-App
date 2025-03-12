plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.chatting_app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.chatting_app"
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
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures{
        viewBinding = true
        dataBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.location)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    //noinspection UseTomlInstead,GradleDependency
    implementation ("com.google.android.gms:play-services-auth:20.7.0")
    //noinspection UseTomlInstead
    implementation(platform("com.google.firebase:firebase-bom:33.8.0"))
    //noinspection UseTomlInstead
    implementation ("com.google.firebase:firebase-auth:23.1.0")
    //noinspection UseTomlInstead
    implementation("com.google.firebase:firebase-auth-ktx")
    //noinspection UseTomlInstead
    implementation ("com.google.firebase:firebase-database:21.0.0")
    //noinspection UseTomlInstead
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    //noinspection UseTomlInstead
    implementation ("com.google.firebase:firebase-firestore:25.1.1")
    //noinspection UseTomlInstead
    implementation ("com.google.firebase:firebase-storage:21.0.1")
    //noinspection UseTomlInstead
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation ("com.google.android.gms:play-services-location:21.2.0")

    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.5.1")
    implementation ("com.google.android.material:material:1.9.0")


    val nav_version = "2.8.5"

    // Views/Fragments integration
    //noinspection KtxExtensionAvailable,UseTomlInstead,GradleDependency
    implementation("androidx.navigation:navigation-fragment:$nav_version")
    //noinspection GradleDependency,UseTomlInstead,KtxExtensionAvailable
    implementation("androidx.navigation:navigation-ui:$nav_version")
}