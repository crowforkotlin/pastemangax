buildscript {

    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.10")
        classpath("com.android.tools.build:gradle:7.4.2")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.5.3")
    }

}

allprojects {

    repositories {
        google()
        mavenCentral()
        maven { setUrl("https://maven.aliyun.com/nexus/content/groups/public/") }
        maven { setUrl("https://jitpack.io") }
    }

}