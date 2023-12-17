@file:Suppress("UnstableApiUsage")

rootProject.name = "CopyMangaX"
include(":app")
include(":lib_base")
include(":lib_mangax")
include(":module_home")
include(":module_main")
include(":module_discover")
include(":module_bookshelf")
include(":module_book")
include(":module_mine")
include(":module_anime")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { setUrl("https://jitpack.io") }
    }
}


dependencyResolutionManagement {

    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    versionCatalogs {
        create("compose") { from(files("gradle/compose.versions.toml")) }
        create("app") { from(files("gradle/app.versions.toml") )}
    }

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { setUrl("https://plugins.gradle.org/m2/") }
        maven { setUrl("https://maven.aliyun.com/nexus/content/groups/public/") }
        maven { setUrl("https://jitpack.io") }
    }
}
