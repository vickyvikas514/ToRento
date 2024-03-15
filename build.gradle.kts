buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.0")
        classpath ("com.android.tools.build:gradle:8.3.0")
        classpath ("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21")
    }
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.3.0" apply false
    id("org.jetbrains.kotlin.android") version "1.8.0" apply false
    id ("io.realm.kotlin") version "1.11.0" apply  false
}

