plugins {

    // 使用插件库
    id(Plugins.android_library)

    // Ksp
    kotlin(Plugins.kotlin_kapt)

    // 使用 Kotlin语言开发Android 插件
    kotlin(Plugins.kotlin_android)

}

android {

    // 标识应用程序命名空间 （应用商店上的唯一标识符）
    namespace = AppConfigs.base_namespace

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
        resourcePrefix(AppConfigs.base_resource_prefix)

        // 编译的SDK版本
        compileSdk = AppConfigs.compile_sdk_version

        // 兼容最小版本的SDK
        minSdk = AppConfigs.min_sdk_version

        // 兼容目标版本的SDK
        targetSdk = AppConfigs.target_sdk_version

        ndk {
            // 设置支持的SO库架构 'x86', 'armeabi-v7a', 'x86_64', 'arm64-v8a'
            abiFilters.add("armeabi")
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
        jniLibs.srcDirs(AppConfigs.source_libs, AppConfigs.source_jniLibs)
    }
    composeOptions {
        kotlinCompilerExtensionVersion = compose.versions.compiler.get()
    }
}

kotlin { jvmToolchain(11) }

dependencies {

    // 引入App下libs文件下的所有Jar包
    api(fileTree("dir" to "libs", "include" to "*.jar"))

    /* Home */
    api(Dependencies.androidx_core)
    api(Dependencies.androidx_activity)
    api(Dependencies.androidx_appcompat)
    api(Dependencies.android_material)
    api(Dependencies.androidx_constraintlayout)
    api(Dependencies.androidx_lifecycle_ktx)
    api(Dependencies.androidx_lifecycle_livedata_ktx)
    api(Dependencies.androidx_lifecycle_service)
    api(Dependencies.androidx_lifecycle_viewmodel_ktx)
    api(Dependencies.androidx_datastore)
    api(Dependencies.androidx_paging_runtime)
    api(Dependencies.androidx_paging_runtime_ktx)
    api(Dependencies.androidx_paging_common)
    api(Dependencies.androidx_paging_common_ktx)
    api(Dependencies.androidx_core_splash_screen)

    debugApi(Dependencies.glance)
    debugApi(Dependencies.leakcanary) // 不支持在MinSdk 24以下的设备运行
    testApi(Dependencies.junit_junit)
    androidTestApi(Dependencies.androidx_test_junit_ktx)
    androidTestApi(Dependencies.androidx_test_espresso)

    /* Kotlin 协程 */
    api(Dependencies.kotlinx_coroutines)
    api(Dependencies.kotlinx_datetime)///
    api(Dependencies.kotlin_stdlib)
    api(Dependencies.kotlin_reflect)

    api(Dependencies.androidx_room_runtime)
    api(Dependencies.androidx_room_ktx)

    /* Koin 注入框架 */
    api(Dependencies.android_koin)

    /* Github */
    api(Dependencies.retrofit)
    api(Dependencies.retrofit_scalars)
    api(Dependencies.retrofit_moshi)

    api(Dependencies.moshi)

    api(Dependencies.okhttp)
    api(Dependencies.okhttp_loggin)

    api(Dependencies.logger)
    api(Dependencies.lottie)
    api(Dependencies.photoview)

    api(Dependencies.glide)
    api(Dependencies.glide_integration) { exclude(group = "glide-parent") }
    kapt(Dependencies.glide_compiler)

    api(Dependencies.zguop_banner)
    api(Dependencies.tencent_bugly)
    api(Dependencies.loading_button)
    api(Dependencies.luksiege_picture_selector)
    api(Dependencies.luksiege_ucrop)

    api(Dependencies.smart_refresh)
    api(Dependencies.smart_refresh_material_header)

    api(platform(compose.bom))
    api(compose.activity)
    api(compose.material3.core)
    api(compose.material.core)
    api(compose.accompanist.themeadapter)
    api(compose.ui.tooling)
    api(compose.material.icons)

}