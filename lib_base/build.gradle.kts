plugins {
    alias(app.plugins.android.library)
    alias(app.plugins.android.kotlin)
    alias(app.plugins.android.ksp)
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
        resourcePrefix = AppConfigs.base_resource_prefix

        // 编译的SDK版本
        compileSdk = AppConfigs.compile_sdk_version

        // 兼容最小版本的SDK
        minSdk = AppConfigs.min_sdk_version

        // 兼容目标版本的SDK
        targetSdk = AppConfigs.target_sdk_version

        ndk {
            // 设置支持的SO库架构 'x86', 'armeabi-v7a', 'x86_64', 'arm64-v8a'
            abiFilters += listOf("armeabi", "x86", "x86_64", "armeabi-v7a", "armeabi-v8a")
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

    // 配置Compose 选项
    composeOptions {

        // 设置 Kotlin Compose 编译器扩展的版本 （Important）
        kotlinCompilerExtensionVersion = compose.versions.compiler.get()
    }
}

dependencies {

    // 引入App下libs文件下的所有Jar包
    api(fileTree("dir" to "libs", "include" to "*.jar"))

    api(app.androidx.core)
    api(app.androidx.activity)
    api(app.androidx.appcompat)
    api(app.androidx.material)
    api(app.androidx.constraintlayout)
    api(app.androidx.lifecycle.service)
    api(app.androidx.lifecycle.runtime.ktx)
    api(app.androidx.lifecycle.livedata.ktx)
    api(app.androidx.lifecycle.viewmodel.ktx)
    api(app.androidx.datastore.preferences)
    api(app.androidx.paging.runtime.ktx)
    api(app.androidx.paging.common.ktx)
    api(app.androidx.core.splashscreen)
    api(app.androidx.room.ktx)
    api(app.androidx.room.runtime)

    api(libs.kotlin.coroutines.core)
    api(libs.kotlin.stdlib)
    api(libs.moshi)
    api(libs.moshi.kotlin)
    api(libs.okhttp)
    api(libs.okhttp.logging.interceptor)
    api(libs.retrofit)
    api(libs.retrofit.converter.moshi)
    api(libs.koin.android)
    api(libs.logger)
    api(libs.lottie)
    api(libs.photoview)
    api(libs.loading.button)
    api(libs.luksiege.picture.selector)
    api(libs.luksiege.ucrop)
    api(libs.refresh.layout.kernel)
    api(libs.refresh.header.material)
    api(libs.glide)
    api(libs.glide.okhttp3.integration) { exclude(group = "glide-parent") }
    ksp(libs.glide.ksp)

    api(platform(compose.bom))
    api(compose.androidx.activity)
    api(compose.androidx.material3)
    api(compose.androidx.material)
    api(compose.androidx.ui.tooling)
    api(compose.androidx.ui.util)
    api(compose.androidx.material.icons.extended)
    api(compose.accompanist.themeadapter)
    api(compose.accompanist.systemuicontroller)

    // 不支持在MinSdk 24以下的设备运行
    debugApi(libs.leakcanary.android)
    debugApi(libs.glance)
    androidTestApi(app.androidx.test.junit.ktx)
    androidTestApi(app.androidx.test.espresso.core)
}