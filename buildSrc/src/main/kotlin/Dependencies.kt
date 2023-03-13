/*************************
 * @Machine: RedmiBook Pro 15
 * @RelativePath: Configuration.kt
 * @Path: D:\Barry\B_study\Android\Android_Project\JetpackApp\buildSrc\src\main\kotlin\Configuration.kt
 * @Author: CrowForKotlin
 * @Time: 2022/5/6 9:18 周五 上午
 * @Description: 项目配置 依赖
 * @formatter:off
 *************************/

object Dependencies {

    /* MultiDex
    * - Android Home : [https://developer.android.google.cn/studio/build/multidex?hl=zh_cn]
    * */
    const val androidx_multidex = "androidx.multidex:multidex:2.0.1"

    /* 针对最新的平台功能和 API 调整应用,同时还支持旧设备
    * - Jetpack : [https://developer.android.google.cn/jetpack/androidx/releases/core?hl=zh-cn]
    * */
    const val androidx_core = "androidx.core:core-ktx:${Versions.core_version}"

    /* 允许在平台的旧版 API 上访问新 API（很多使用 Material Design）*/
    const val androidx_appcompat = "androidx.appcompat:appcompat:1.5.1"

    /* 访问基于 Activity 构建的可组合 API */
    const val androidx_activity = "androidx.activity:activity-ktx:1.6.1"

    /* JUnit 是一个编写可重复测试的简单框架。它是用于单元测试框架的 xUnit 架构的一个实例
    * - Github : [https://github.com/junit-team/junit4]
    * */
    const val junit_junit = "junit:junit:4.13.2"

    /* 在 Android 中进行测试 */

    const val androidx_test_junit_ktx = "androidx.test.ext:junit-ktx:1.1.5"
    const val androidx_test_espresso = "androidx.test.espresso:espresso-core:3.4.0"

    /* androidx lifecycle */
    const val androidx_lifecycle_ktx = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycle_version}"
    const val androidx_lifecycle_service = "androidx.lifecycle:lifecycle-service:${Versions.lifecycle_version}"
    const val androidx_lifecycle_viewmodel_ktx = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle_version}"
    const val androidx_lifecycle_livedata_ktx = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifecycle_version}"

    /** androidx paging */
    const val androidx_paging_runtime = "androidx.paging:paging-runtime:${Versions.paging_version}"
    const val androidx_paging_common = "androidx.paging:paging-common:${Versions.paging_version}"
    const val androidx_paging_rxjava2 = "androidx.paging:paging-rxjava2:${Versions.paging_version}"
    const val androidx_paging_runtime_ktx = "androidx.paging:paging-runtime-ktx:${Versions.paging_version}"
    const val androidx_paging_common_ktx = "androidx.paging:paging-common-ktx:${Versions.paging_version}"
    const val androidx_paging_rxjava2_ktx = "androidx.paging:paging-rxjava2-ktx:${Versions.paging_version}"

    /* 使用“相对定位(约束布局)”灵活地确定控件的位置和大小 */
    const val androidx_constraintlayout = "androidx.constraintlayout:constraintlayout:2.1.4"

    /* Palette 获取颜色可以用于沉浸式 */
    const val androidx_palette = "androidx.palette:palette:1.0.0"

    /* Material Design 界面组件
    * - MvnRepository : [https://mvnrepository.com/artifact/com.google.android.material/material]
    * */
    const val android_material = "com.google.android.material:material:1.9.0-alpha02"

    /* Kotlin Android 注入库
    * - Home : [https://insert-koin.io/]
    * */
    const val android_koin = "io.insert-koin:koin-android:3.2.0"

    /* DataStore 替换 SharedPreference */
    const val androidx_datastore = "androidx.datastore:datastore-preferences:1.0.0"

    /* Coroutine 协程
    * - GitHub : [https://github.com/Kotlin/kotlinx.coroutines]
    * */
    const val kotlinx_coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4"

    /* SmartRefresh
    * - GitHub : [https://github.com/scwang90/SmartRefreshLayout]
    * */
    const val smart_refresh = "io.github.scwang90:refresh-layout-kernel:${Versions.smart_refresh}" // 核心依赖
    const val smart_refresh_header = "io.github.scwang90:refresh-header-classics:${Versions.smart_refresh}" //经典刷新头
    const val smart_refresh_radar = "io.github.scwang90:refresh-header-radar:${Versions.smart_refresh}"     //雷达刷新头

    /* Swiperefreshlayout */
    const val androidx_swiperefreshlayout = "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0"

    /* navigation fragment 导航碎片
    * - Home : [https://developer.android.google.cn/jetpack/androidx/releases/navigation?hl=en]
    * */
    const val androidx_navigation_fragment_ktx = "androidx.navigation:navigation-fragment-ktx:${Versions.navigation_version}"
    const val androidx_navigation_ui_ktx = "androidx.navigation:navigation-ui-ktx:${Versions.navigation_version}"

    const val androidx_core_splash_screen = "androidx.core:core-splashscreen:1.0.0-beta02"

    /* Retrofit 库依赖
    * - Github: [https://github.com/square/retrofit]
    * */
    const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit_version}"
    const val retrofit_scalars = "com.squareup.retrofit2:converter-scalars:${Versions.retrofit_version}"
    const val retrofit_gson = "com.squareup.retrofit2:converter-gson:${Versions.retrofit_version}"
    const val retrofit_moshi = "com.squareup.retrofit2:converter-moshi:${Versions.retrofit_version}"

    /* Moshi
    * - GitHub : [https://github.com/square/moshi]
    * */
    const val moshi = "com.squareup.moshi:moshi-kotlin:${Versions.moshi_version}"

    /* ARouter 路由
    * - Github: [https://github.com/alibaba/ARouter]
    * */
    const val arouter_api = "com.alibaba:arouter-api:${Versions.arouter_version}"
    const val arouter_compiler = "com.alibaba:arouter-compiler:${Versions.arouter_version}"

    /* kotlin Json 序列化
    * - Github: [https://github.com/Kotlin/kotlinx.serialization]
    * */
    const val kotlin_serialization = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1"


    /* okhttp 网络Http客户端
    * - Github : [https://github.com/square/okhttp]
    * */
    const val okhttp = "com.squareup.okhttp3:okhttp:${Versions.okhttp_version}"
    const val okhttp_urlconnection = "com.squareup.okhttp3:okhttp-urlconnection:${Versions.okhttp_version}"
    const val okhttp_loggin = "com.squareup.okhttp3:logging-interceptor:${Versions.okhttp_version}"

    /* Logger 日志打印
    * - Github: [https://github.com/orhanobut/logger]
    * */
    const val logger = "com.orhanobut:logger:2.2.0"

    /* lottie动画
    * - Github: [https://github.com/airbnb/lottie-android]
    * */
    const val lottie = "com.airbnb.android:lottie:5.1.1"


    /* leakcanary 内存泄漏检查工具
    * - Github : [https://github.com/square/leakcanary]
    * */
    const val leakcanary ="com.squareup.leakcanary:leakcanary-android:2.9.1"

    /* Glide 图片加载
    * - Github : [https://github.com/bumptech/glide]
    * */
    const val glide = "com.github.bumptech.glide:glide:${Versions.glide_version}"
    const val glide_compiler = "com.github.bumptech.glide:compiler:${Versions.glide_version}"

    /* PhotoView
    * - Github : [https://github.com/Baseflow/PhotoView]
    * */
    const val photoview = "com.github.chrisbanes:PhotoView:2.0.0"

    /* Volley 网络请求
    * - Home : [https://developer.android.google.cn/training/volley?hl=zh-cn]
    * */
    const val volley = "com.android.volley:volley:1.2.1"

    /* 闪光布局
    * - Github : [https://github.com/team-supercharge/ShimmerLayout]
    * */
    const val shimmerlayout = "io.supercharge:shimmerlayout:2.1.0"

    /* 屏幕适配
    * - Github : [https://github.com/JessYanCoding/AndroidAutoSize]
    * */
    const val autosize = "com.github.JessYanCoding:AndroidAutoSize:v1.2.1"

    /*
    * banner 轮播图
    * - Github: [https://github.com/youth5201314/banner]
    * */
    const val youth_banner = "io.github.youth5201314:banner:2.2.2"

    /*
    * banner 轮播图
    * - Github : [https://github.com/zguop/banner]
    * */
    const val zguop_banner = "io.github.zguop:pager2Banner:1.0.5"

    /*
    * banner 轮播图
    * - Github : [https://github.com/zhpanvip/BannerViewPager]
    * */
    const val zhpanvip_banner = "com.github.zhpanvip:bannerviewpager:3.5.11"

    /* 圆形图片
    * - Github : [https://github.com/lopspower/CircularImageView]
    * */
    const val circular_imageview = "com.mikhaellopez:circularimageview:4.3.0"

    const val ktor_client_core = "io.ktor:ktor-client-core:${Versions.ktor_version}"
    const val ktor_client_cio = "io.ktor:ktor-client-cio:${Versions.ktor_version}"
    const val ktor_client_okhttp = "io.ktor:ktor-client-okhttp:${Versions.ktor_version}"
    const val ktor_client_android = "io.ktor:ktor-client-android:${Versions.ktor_version}"
    const val ktor_client_logging = "io.ktor:ktor-client-logging:${Versions.ktor_version}"
    const val ktor_client_content = "io.ktor:ktor-client-content-negotiation:${Versions.ktor_version}"
    const val ktor_client_json = "io.ktor:ktor-serialization-kotlinx-json:${Versions.ktor_version}"
    const val ktor_client_gson = "io.ktor:ktor-serialization-gson-jvm:${Versions.ktor_version}"

    /* Android 图像加载库
    * - Github : [https://github.com/coil-kt/coil]
    * */
    const val coil_kotlin = "io.coil-kt:coil:2.1.0"

    const val eventbus = "org.greenrobot:eventbus:3.3.1"

    const val immersionBar = "com.geyifeng.immersionbar:immersionbar:3.2.2"
    const val immersionBar_kt = "com.geyifeng.immersionbar:immersionbar-ktx:3.2.2"

    /* 图片加载库
    * - Github : [https://github.com/square/picasso]
    * */
    const val picasso = "com.squareup.picasso:picasso:2.8"


    /* 大图预览 */
    const val bigImageViewPager =  "com.github.SherlockGougou:BigImageViewPager:androidx-7.0.4"

    /* 腾讯质量跟踪服务
    * - Home : [https://bugly.qq.com/v2/workbench/apps]
    * */
    const val tencent_bugly = "com.tencent.bugly:crashreport:latest.release"
}