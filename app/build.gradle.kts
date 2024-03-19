plugins {
    alias(app.plugins.android.application)
    alias(app.plugins.android.kotlin)
    alias(app.plugins.android.ksp)
}

android {

    // 标识应用程序命名空间 （应用商店上的唯一标识符）
    namespace = AppConfigs.namespace

    // 配置构建功能相关的选项
    buildFeatures(Action {

        // 开启 ViewBinding
        viewBinding = true
    })

    // 应用程序的默认配置信息
    defaultConfig {

        // 编译SDK 版本
        compileSdk = AppConfigs.compile_sdk_version

        // 构建工具版本
        buildToolsVersion = AppConfigs.build_tools_version

        // 标识应用程序ID （设备上的唯一标识符）
        applicationId = AppConfigs.application_id

        // 兼容最小版本的SDK
        minSdk = AppConfigs.min_sdk_version

        // 兼容目标版本的SDK
        targetSdk = AppConfigs.target_sdk_version

        // 指定应用程序的内部版本号 （和外部版本号依赖）
        versionCode = AppConfigs.version_code

        // 指定应用程序的外部版本号 （和内部版本号依赖）
        versionName = AppConfigs.version_name

        // 指定运行测试的工具
        testInstrumentationRunner = AppConfigs.test_instrumentation_runner

        // 启用MultiDex
        multiDexEnabled = true

        // 资源前缀（所有资源前缀必须添加）
        resourcePrefix(AppConfigs.app_resource_prefix)

        // NDK默认配置
        ndk {

            // NDK VERSION 设置23 和 MINSDK 23保持一致
            ndkVersion = AppConfigs.ndk_version

            // 设置支持的SO库架构 'x86', 'armeabi-v7a', 'x86_64', 'arm64-v8a'
            abiFilters += listOf("x86", "x86_64", "armeabi-v7a", "arm64-v8a")
        }

        // 设置APK名
        setProperty("archivesBaseName", "CopyMangaX-$versionName")
    }

    // 应用程序的构建类型
    buildTypes {

        // 调试版本
        debug {

            versionNameSuffix = ".debug"

            // 设置ApplicationID
            applicationIdSuffix = ".debug"

            // 关闭代码压缩
            isMinifyEnabled = false

            // 关闭资源深度压缩
            isShrinkResources = false
        }

        // 发行版本
        release {

            // 开启代码压缩
            isMinifyEnabled = true

            // 开启资源深度压缩
            isShrinkResources = true

            // 添加ProGuard配置，优化发行版性能
            proguardFiles(
                getDefaultProguardFile(AppConfigs.proguard_android_optimize_txt),
                AppConfigs.proguard_rules_pro
            )
        }
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

    // （产品口味） 是一个抽象的概念，表示应用程序的不同版本
    productFlavors {

        // 维度 抽象的概念，表示应用程序的不同版本
        flavorDimensions.add(AppConfigs.flavor_dimension)

        // 内部版本
        create(AppConfigs.flavor_internal) {

            // 指定维度
            dimension = AppConfigs.flavor_dimension

            // 版本名后缀
            versionNameSuffix = "_internal"

            // .internal后缀
            applicationIdSuffix = ".internal"

            // Internal 版本号和名称
            versionCode = AppConfigs.version_code_internal

            versionName = AppConfigs.version_name_internal

            // 是否使用线上环境
            buildConfigField("boolean", "IS_ONLINE_ENV", "true")
        }

        // 正式线上版本
        create(AppConfigs.flavor_online) {

            // 指定维度
            dimension = AppConfigs.flavor_dimension

            // 版本名后缀
            versionNameSuffix = versionName

            // 是否使用线上环境
            buildConfigField("boolean", "IS_ONLINE_ENV", "true")
        }

        // 开发版本
        create(AppConfigs.flavor_dev) {

            // 指定维度
            dimension = AppConfigs.flavor_dimension

            // 应用包名后缀
            applicationIdSuffix = ".dev"

            // 版本名后缀
            versionNameSuffix = "_dev"

            // 是否使用线上环境
            buildConfigField("boolean", "IS_ONLINE_ENV", "false")
        }
    }
}

dependencies {

    // 引入lib_base库
    implementation(project(":lib_base"))

    // 引入lib_mangax库
    implementation(project(":lib_mangax"))

    // 模块 主页
    implementation(project(":module_main"))

    // 引入MultiDex依赖
    implementation(app.androidx.multidex)
}