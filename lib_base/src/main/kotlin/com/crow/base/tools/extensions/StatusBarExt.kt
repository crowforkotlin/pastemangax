
@file:SuppressLint("ALL")
@file:Suppress("unused")

package com.crow.base.tools.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Insets
import android.os.Build
import android.view.*
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type.InsetsType
import androidx.core.view.WindowInsetsControllerCompat
import com.crow.base.app.appContext


/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/java/cn/barry/base/view
 * @Time: 2022/5/16 21:37
 * @Author: CrowForKotlin
 * @Description: StatusBarExt
 * @formatter:off
 **************************/
interface IBaseImmersionBarAPI {

    fun ApiIn29To33(insets: Insets)

    fun ApiIn21To28(windowInsets: WindowInsets)
}

/** 设置状态栏的文字图标是否暗色显示 */
fun Activity.setStatusBarIsDark(isDark: Boolean = true) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { /* 大于等于 SDK_API 30 Android 11  */
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = isDark
    } else if (isDark) window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
}

/** 获取顶部状态栏高度 */
fun Context.getStatusBarHeight() : Int{
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    return if (resourceId > 0) appContext.resources.getDimensionPixelSize(resourceId) else 0
}

/** 获取底部导航栏高度 */
fun Context.getNavigationBarHeight() : Int{
    val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
    return if (resourceId > 0) appContext.resources.getDimensionPixelSize(resourceId) else 0
}

/** 隐藏 [状态栏、导航栏、系统栏]*/
fun View.hideBars(@InsetsType type: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        windowInsetsController?.apply {
            hide(type)
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    } else {
        if (type == WindowInsetsCompat.Type.navigationBars() || type == WindowInsetsCompat.Type.systemBars()) {
            systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }
}

/** 隐藏 [状态栏、导航栏、系统栏]*/
fun Window.hideBars(@InsetsType type: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        insetsController?.apply {
            hide(type)
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    } else {
        if (type == WindowInsetsCompat.Type.navigationBars() || type == WindowInsetsCompat.Type.systemBars()) {
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
    }
}

/**
* 全屏沉浸式效果，仅提供主要的参数，主要沉浸效果实现还需自己
* 手动给View设置Padding或者Margin，
*
* @param iBaseImmersionBarAPI 实现两个API版本下的回调的接口
*/
fun Window.immersionSystemBar(iBaseImmersionBarAPI: IBaseImmersionBarAPI) {
    val attrs = attributes
    attrs.flags = attrs.flags and WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION.inv()
    attrs.flags = attrs.flags and WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS.inv()
    attrs.flags.or(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)             // 不限制布局
    attrs.flags.or(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS) // 允许在状态栏区域绘制
    decorView.setOnApplyWindowInsetsListener { _, windowInsets ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            iBaseImmersionBarAPI.ApiIn29To33(windowInsets.getInsets(WindowInsets.Type.systemBars()))
            WindowInsets.CONSUMED
        } else {
            iBaseImmersionBarAPI.ApiIn21To28(windowInsets)
            windowInsets.consumeSystemWindowInsets().consumeStableInsets()
        }
    }
}

/**
 * 设置 内边距属性 实现沉浸式效果
 *
 * @param paddingStatusBar (Defualt) true
 * @param paddingNaviateBar (Default) true
 */
fun immersionPadding(view: View, paddingStatusBar: Boolean = true, paddingNaviateBar: Boolean = true) {
    view.setPadding(0, if (paddingStatusBar) view.context.getStatusBarHeight() else 0, 0, if(paddingNaviateBar) view.context.getNavigationBarHeight() else 0)
}

/**
 * ● 沉浸式（推荐）
 *
 * ● 2023-07-08 02:08:38 周六 上午
 */
inline fun immersionPadding(root: View, crossinline update: (v: View, insets: androidx.core.graphics.Insets, gestureInsets: androidx.core.graphics.Insets) -> Unit) {
    ViewCompat.setOnApplyWindowInsetsListener(root) { view: View, windowInsetsCompat: WindowInsetsCompat ->
        update(view, windowInsetsCompat.getInsets(WindowInsetsCompat.Type.systemBars()), windowInsetsCompat.getInsets(WindowInsetsCompat.Type.systemGestures()))
        WindowInsetsCompat.CONSUMED
    }
}

/**
 * ● 全屏沉浸式
 *
 * ● 2023-06-27 00:51:18 周二 上午
 */
fun immersionFullScreen(windowInsetsControllerCompat: WindowInsetsControllerCompat) {
    windowInsetsControllerCompat.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    windowInsetsControllerCompat.hide(WindowInsetsCompat.Type.systemBars())
}



fun immerureCutoutCompat(window: Window) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
    }
}

fun immersureFullView(window: Window, fitView: Boolean = false) = WindowCompat.setDecorFitsSystemWindows(window, fitView)
