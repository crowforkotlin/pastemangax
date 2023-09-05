buildscript {

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }

    dependencies {
        classpath(kotlin("gradle-plugin", version = Versions.kotlin_version))
        classpath(kotlin("serialization", version = Versions.kotlin_version))
        classpath("com.android.tools.build:gradle:8.3.0-alpha01")

    }

}
allprojects { apply(plugin = "kotlinx-serialization") }

/*
allprojects {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://plugins.gradle.org/m2/") }
        maven { setUrl("https://maven.aliyun.com/nexus/content/groups/public/") }
        maven { setUrl("https://jitpack.io") }
    }
}
*/
