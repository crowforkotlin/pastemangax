plugins {
    alias(app.plugins.android.library)
    alias(app.plugins.android.kotlin)
    alias(app.plugins.android.ksp)
}

android {

    // 标识应用程序命名空间 （应用商店上的唯一标识符）
    namespace = AppConfigs.lib_copymanga

    // 配置构建功能相关的选项
    buildFeatures {

        // 开启 ViewBinding
        viewBinding = true

        // 开启 Compose
        compose = true
    }

    defaultConfig {

        buildConfigField("String", "APPLICATION_ID", "\"${AppConfigs.application_id}\"")
        buildConfigField("String", "VERSION_NAME", "\"${AppConfigs.version_name}\"")
        buildConfigField("int", "VERSION_CODE", "${AppConfigs.version_code}")

        // 资源前缀（所有资源前缀必须添加）
        resourcePrefix = AppConfigs.mangax_resource_prefix

        // 编译的SDK版本
        compileSdk = AppConfigs.compile_sdk_version

        // 兼容最小版本的SDK
        minSdk = AppConfigs.min_sdk_version

        // 兼容目标版本的SDK
        targetSdk = AppConfigs.target_sdk_version

        // NDK默认配置
        ndk {

            // NDK VERSION 设置23 和 MINSDK 23保持一致
            ndkVersion = AppConfigs.ndk_version

            // 设置支持的SO库架构 'x86', 'armeabi-v7a', 'x86_64', 'arm64-v8a'
//            abiFilters += listOf("x86", "x86_64", "armeabi-v7a", "arm64-v8a")
        }
    }

    // 打包时提供不同CPU架构的APK
    splits {
        abi {

            // 清除ABI的所有配置
            reset()

            // 启用
            isEnable = true

            // 不集成 到单独一个APK，由于架构有4个，会增加APK体积
            isUniversalApk = false
            include("x86", "x86_64", "armeabi-v7a", "arm64-v8a")
        }
    }

    // NDK BUILD构建配置
    externalNativeBuild {
        cmake {
            path = file("CMakeLists.txt")
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

    // 配置应用程序的源代码集
    sourceSets.named(AppConfigs.source_set_main) {

        // Java 源代码路径
        java.srcDirs(AppConfigs.source_java)

        // Kotlin 源代码路径
        kotlin.srcDir(AppConfigs.source_kotlin)

        // JNI 库文件路径
        jniLibs.srcDirs(AppConfigs.source_libs)
    }

    // 配置Compose 选项
    composeOptions {

        // 设置 Kotlin Compose 编译器扩展的版本 （Important）
        kotlinCompilerExtensionVersion = compose.versions.compiler.get()
    }
}

dependencies {

    // 引入App下libs文件下的所有Jar包
    api(fileTree("dir" to "libs", "include" to "*.jar"))

    // Base 库
    implementation(project(":lib_base"))

    api(libs.subsampling.scale.image.view)
}