@file:Suppress("unused")

package com.crow.base.tools.extensions

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.ViewPropertyAnimator


const val BASE_ANIM_300L = 300L
const val BASE_ANIM_200L = 200L

/*
在进行 `dp` 到 `px` 的转换时，数值通常会变大，因为 `dp` 是设备无关像素，而 `px` 是实际的物理像素点，
其数量是根据屏幕的分辨率而定的。当设备的像素密度比较高时，`dp` 像素点所占据的实际像素点数量就会更多，因此在将 `dp` 像素点转换为实际像素点时，数值会相应地变大。
在进行 `px` 到 `dp` 的转换时，数值通常会变小，因为 `dp` 像素点在高像素密度的设备上所占据的实际像素点数量比较多，因此在将实际像素点转换为 `dp` 像素点时，数值会相应地变小。
需要注意的是，在进行数值转换时，还需要考虑到取整偏移的影响，因为在进行浮点数计算时可能会出现精度损失的问题。
因此，在进行数值转换时，需要将浮点数运算的结果进行四舍五入，并加上 0.5 或者 0.5f 的偏移量来确保计算结果的精确性。
* */
// 工具类 提供px和dip的相互转化
fun Context.dp2px(dp: Float): Int {
    return (dp * resources.displayMetrics.density + 0.5).toInt()
}
fun Context.px2dp(px: Float): Float {
    return px / resources.displayMetrics.density + 0.5f
}

fun Context.dip2px(dp: Float): Int {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics).toInt()
}

fun Context.px2dip(px: Float): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, resources.displayMetrics) / resources.displayMetrics.density + 0.5f
}

// 淡入
fun View.animateFadeIn(duration: Long = BASE_ANIM_200L): ViewPropertyAnimator {
    alpha = 0f
    visibility = View.VISIBLE
    return animate().alpha(1f).setDuration(duration)
}

//淡出
fun View.animateFadeOut(duration: Long = BASE_ANIM_300L): ViewPropertyAnimator {
    alpha = 1f
    visibility = View.VISIBLE
    return animate().alpha(0f).setDuration(duration)
}