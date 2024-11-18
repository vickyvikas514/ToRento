import com.android.build.api.dsl.Packaging
import org.jetbrains.kotlin.utils.addToStdlib.ifFalse
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id ("io.realm.kotlin")
}
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")

if (localPropertiesFile.exists()) {
    localPropertiesFile.reader(Charsets.UTF_8).use { reader ->
        localProperties.load(reader)
    }
}

android {
    namespace = "com.example.torento"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.torento"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        //API
        val apiKey = localProperties.getProperty("apiKey", "")
        buildConfigField("String", "API_KEY", "\"$apiKey\"")


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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true

    }
    kotlinOptions {
        jvmTarget = "17"

    }
    buildFeatures{
        buildConfig = true
        viewBinding = true
    }
    packagingOptions {
        resources.excludes.add("META-INF/native-image/org.mongodb/bson/native-image.properties")
    }
}


dependencies {

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("com.google.firebase:firebase-auth-ktx:23.1.0")
    implementation("com.google.firebase:firebase-firestore-ktx:25.1.1")
    implementation("com.google.firebase:firebase-storage:21.0.1")
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.google.firebase:firebase-firestore:25.1.1")
    implementation("com.google.firebase:firebase-database:21.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation ("io.realm.kotlin:library-sync:1.11.0")
    implementation(platform("com.google.firebase:firebase-bom:33.6.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation ("org.jetbrains.kotlin:kotlin-reflect:1.9.20")
    //for AI integration
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    //Lottie Animation
    implementation ("com.airbnb.android:lottie:6.4.1")
    // Also add the dependency for the Google Play services library and specify its version
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation ("com.google.android.gms:play-services-location:21.3.0")
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.3")
}