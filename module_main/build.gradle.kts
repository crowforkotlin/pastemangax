plugins {
    // Android 应用程序插件
    id(Plugins.android_library)

    // Ksp
    // id(Plugins.google_devtools_ksp) version Versions.ksp_version

    // 使用 Kotlin语言开发Android 插件
    kotlin(Plugins.kotlin_android)

    // 使用Kotlin序列化插件
    // kotlin(Plugins.kotlin_serialization) version Versions.kotlin_version
}

android {

    // 配置构建功能相关的选项
    buildFeatures {

        // 开启 ViewBinding
        viewBinding = true
    }

    // 应用程序的默认配置信息
    defaultConfig {

        // 编译SDK 版本
        compileSdk = AppConfigs.compile_sdk_version

        // 标识应用程序命名空间 （应用商店上的唯一标识符）
        namespace = AppConfigs.module_main_namespace

        // 资源前缀（所有资源前缀必须添加）
        resourcePrefix = AppConfigs.main_resource_prefix

        // 构建工具版本
        buildToolsVersion = AppConfigs.build_tools_version

        // 兼容最小版本的SDK
        minSdk = AppConfigs.min_sdk_version

        // 兼容目标版本的SDK
        targetSdk = AppConfigs.target_sdk_version

        // 指定运行测试的工具
        testInstrumentationRunner = AppConfigs.test_instrumentation_runner
    }

    // Android Gradle 构建时的编译选项
    compileOptions {

        // 源代码兼容性 Java 11 （使用Java 11特性）
        sourceCompatibility = JavaVersion.VERSION_11

        // 目标代码兼容性 Java 11 （指定可运行的Java版本兼容性）
        targetCompatibility = JavaVersion.VERSION_11
    }

    // 配置 Kotlin 编译器
    kotlinOptions {

        // 目标Jvm版本， 编译器生成的字节码可在目标版本上运行
        jvmTarget = AppConfigs.jvm_target

        // 指定编译器的命令行参数 可启用额外功能
        freeCompilerArgs = AppConfigs.free_compile_args
    }
}

dependencies {

    // 引入Base库
    implementation(project(mapOf("path" to ":lib_base")))

    api(project(mapOf("path" to ":module_home")))
    api(project(mapOf("path" to ":module_discover")))
    api(project(mapOf("path" to ":module_bookshelf")))
    api(project(mapOf("path" to ":module_book")))
    api(project(mapOf("path" to ":module_user")))

}