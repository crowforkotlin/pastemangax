/*************************
 * @Machine: RedmiBook Pro 15
 * @RelativePath: Configuration.kt
 * @Path: D:\Barry\B_study\Android\Android_Project\JetpackApp\buildSrc\src\main\kotlin\Configuration.kt
 * @Author: CrowForKotlin
 * @Time: 2022/5/6 9:18 周五 上午
 * @Description: 项目配置 应用配置
 * @formatter:on
 *************************/
object AppConfigs {

    // 命名空间
    val namespace get() = application_id

    const val module_home_namespace = "com.crow.module_home"

    const val module_main_namespace = "com.crow.module_main"

    const val module_discovery_namespace = "com.crow.module_discovery"

    const val module_bookshelf_namespace = "com.crow.module_bookshelf"

    const val module_comic_namespace = "com.crow.module_comic"

    const val module_user_namespace = "com.crow.module_user"

    // Base库的命名空间
    const val base_namespace = "com.crow.base"

    // App 资源前缀
    const val app_resource_prefix = "app"

    // Base库资源前缀
    const val base_resource_prefix = "base"

    // module home 资源前缀
    const val home_resource_prefix = "home"

    // module_main资源前缀
    const val main_resource_prefix = "main"

    // module_discovery资源前缀
    const val discovery_resource_prefix = "discovery"

    // module_bookshelf资源前缀
    const val bookshelf_resource_prefix = "bookshelf"

    // module_comic资源前缀
    const val comic_resource_prefix = "comic"

    // module_user资源前缀
    const val user_resource_prefix = "user"

    // 应用ID
    const val application_id = "com.crow.copymanga"

    // 编译SDK版本 Android 13 T
    const val compile_sdk_version = 33

    // 编译工具版本
    const val build_tools_version = "33.0.0"

    // 最小支持版本 Android 6
    const val min_sdk_version = 23

    // 目标支持版本 Android 13 T
    const val target_sdk_version = 33

    // 应用版本号
    const val version_code = 1

    // 应用版本名
    const val version_name = "1.0.0"

    // jvm版本
    const val jvm_target = "11"

    // Android 提供的默认测试工具
    const val test_instrumentation_runner = "androidx.test.runner.AndroidJUnitRunner"

    // 一个默认的 ProGuard 规则文件，该文件包含了 Android 平台的优化规则
    const val proguard_android_optimize_txt = "proguard-android-optimize.txt"

    // 自定义的 ProGuard 规则文件
    const val proguard_rules_pro = "proguard-rules.pro"

    // 指定名为version 的维度 （一个抽象的概念）表示应用程序的不同版本
    const val flavor_dimension = "version"

    // 正式线上版本
    const val flavor_online = "online"

    // 开发版本
    const val flavor_dev = "dev"

    // 设置代码源名称
    const val source_set_main = "main"

    // 添加到jni库的jniLibs文件
    const val source_jniLibs = "jniLibs"

    // 添加到jni库的libs文件
    const val source_libs = "libs"

    // Java 代码源路径
    const val source_java = "src/main/java"

    // Kotlin 代码源路径
    const val source_kotlin = "src/main/kotlin"

    // 指定编译器的命令行参数 启用额外功能 (ContextReceiver)
    val free_compile_args = listOf("-Xcontext-receivers")

}