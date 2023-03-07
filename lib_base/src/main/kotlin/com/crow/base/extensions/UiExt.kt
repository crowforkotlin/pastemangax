package com.crow.base.extensions

import android.content.Context

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