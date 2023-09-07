buildscript {

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }

    dependencies {
        classpath(kotlin("gradle-plugin", version = Versions.kotlin_version))
        classpath("com.android.tools.build:gradle:8.3.0-alpha01")
    }
}