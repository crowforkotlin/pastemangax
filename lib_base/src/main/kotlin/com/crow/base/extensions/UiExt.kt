@file:Suppress("unused")

package com.crow.base.extensions

import android.content.Context
import android.view.View
import android.view.ViewPropertyAnimator

// 工具类 提供px和dip的相互转化
fun Context.dip2px(dpValue: Float): Int {
    return (dpValue * resources.displayMetrics.density + 0.5f).toInt()
}

fun Context.px2dip(pxValue: Float): Int {
    return (pxValue / resources.displayMetrics.density + 0.5f).toInt()
}

fun Context.dp2px(dp: Int): Int {
    return (dp * resources.displayMetrics.density + 0.5).toInt()
}

fun Context.px2dp(px: Int): Int {
    return (px.toDouble() / resources.displayMetrics.density + 0.5).toInt()
}

// 淡入
fun View.animateFadeIn(duration: Long = 200L): ViewPropertyAnimator {
    alpha = 0f
    visibility = View.VISIBLE
    return animate().alpha(1f).setDuration(duration)
}

//淡出
fun View.animateFadeOut(duration: Long = 200L): ViewPropertyAnimator {
    alpha = 1f
    visibility = View.VISIBLE
    return animate().alpha(0f).setDuration(duration)
}