/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @RelativePath: Plugins.kt
 * @Path: D:\Programing\Android\2023\InterView\buildSrc\src\main\kotlin\Plugins.kt
 * @Author: Crow
 * @Time: 2022/12/28 0:00 周三 上午
 * @Description: Plugins
 * @formatter:on
 *************************/

object Plugins {

    // Android 应用程序插件
    const val android_application = "com.android.application"

    // Android Libiary（库）插件库 不能单独生成可执行的 APK 文件，而是用于生成一个名为 AAR 的库文件，其中包含了库的代码和资源，供其他项目使用。
    const val android_library = "com.android.library"

    // Ksp
    const val google_devtools_ksp = "com.google.devtools.ksp"

    // 使用Kotlin 语言开发Android 插件
    const val kotlin_android = "android"

    const val androidx_navigation_safeargs = "androidx.navigation.safeargs"

    // Kotlin 序列化插件
    const val kotlin_serialization = "plugin.serialization"
}