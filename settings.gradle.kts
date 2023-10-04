rootProject.name = "CopyMangaX"
include(":app")
include(":lib_base")
include(":module_home")
include(":module_main")
include(":module_discover")
include(":module_bookshelf")
include(":module_book")
include(":module_user")

dependencyResolutionManagement {

    versionCatalogs {
        create("compose") {
            from(files("gradle/compose.versions.toml"))
        }
    }
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://plugins.gradle.org/m2/") }
        maven { setUrl("https://maven.aliyun.com/nexus/content/groups/public/") }
        maven { setUrl("https://jitpack.io") }
    }
}
include(":module_anime")