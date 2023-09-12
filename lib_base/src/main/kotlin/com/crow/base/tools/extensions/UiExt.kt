@file:Suppress("unused")

package com.crow.base.tools.extensions

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import android.view.ViewPropertyAnimator
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.crow.base.app.appContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

const val BASE_ANIM_300L = 300L
const val BASE_ANIM_200L = 200L
const val BASE_ANIM_100L = 100L

val appScreenRatio: Float by lazy { appContext.resources.displayMetrics.widthPixels.toFloat() / appContext.resources.displayMetrics.heightPixels.toFloat()  }

fun interface IBaseUiCanvasEvent { fun doOnCanvas(canvas: Canvas) }

/**
* ● 在进行 `dp` 到 `px` 的转换时，数值通常会变大，因为 `dp` 是设备无关像素，而 `px` 是实际的物理像素点，
* 其数量是根据屏幕的分辨率而定的。当设备的像素密度比较高时，`dp` 像素点所占据的实际像素点数量就会更多，因此在将 `dp` 像素点转换为实际像素点时，数值会相应地变大。
* 在进行 `px` 到 `dp` 的转换时，数值通常会变小，因为 `dp` 像素点在高像素密度的设备上所占据的实际像素点数量比较多，因此在将实际像素点转换为 `dp` 像素点时，数值会相应地变小。
* 需要注意的是，在进行数值转换时，还需要考虑到取整偏移的影响，因为在进行浮点数计算时可能会出现精度损失的问题。
* 因此，在进行数值转换时，需要将浮点数运算的结果进行四舍五入，并加上 0.5 或者 0.5f 的偏移量来确保计算结果的精确性。
*
* ● 工具类 提供px和dip的相互转化
 */
fun Context.dp2px(dp: Float): Float {
    return (dp * resources.displayMetrics.density + 0.5f)
}
fun Context.px2dp(px: Float): Float {
    return px / resources.displayMetrics.density + 0.5f
}

fun Context.px2sp(px: Float): Float {
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

fun View.animateFadeOutWithEndInVisibility(duration: Long = BASE_ANIM_300L): ViewPropertyAnimator {
    alpha = 1f
    visibility = View.VISIBLE
    return animate().alpha(0f).setDuration(duration).withEndAction { isInvisible = true }
}

fun View.animateFadeOutWithEndInVisible(duration: Long = BASE_ANIM_300L): ViewPropertyAnimator {
    alpha = 1f
    visibility = View.VISIBLE
    return animate().alpha(0f).setDuration(duration).withEndAction { isVisible = false }
}


suspend fun View.suspendAnimateFadeIn(duration: Long = BASE_ANIM_200L) = suspendCancellableCoroutine { continuation ->
    alpha = 0f
    visibility = View.VISIBLE
    animate().alpha(1f).setDuration(duration).also { it.withEndAction { continuation.resume(it) } }
}

suspend fun View.suspendAnimateFadeOut(duration: Long = BASE_ANIM_300L) = suspendCancellableCoroutine { continuation ->
    alpha = 1f
    visibility = View.VISIBLE
    animate().alpha(0f).setDuration(duration).also { it.withEndAction { continuation.resume(it) } }
}

// 创建画笔
@JvmOverloads
fun View.createPaint(colorString: String? = null, @ColorInt color: Int? = null): Paint = Paint().utilReset(colorString, color)

// 自定义画笔重置方法
@JvmOverloads
fun Paint.utilReset(colorString: String? = null, @ColorInt color: Int? = null) : Paint {
    this.reset()                            // 清空
    this.color = color ?: Color.parseColor(colorString ?: "#FFFFFF")    //这里默认值使用白色，可处理掉系统渲染抗锯齿时，人眼可观察到像素颜色
    this.isAntiAlias = true           // 开启抗锯齿效果
    this.style = Paint.Style.FILL   // 填充样式
    this.strokeWidth = 0f           // 线宽 0
    return this
}

// 扩展获取绘制文字时在x轴上 垂直居中的y坐标
fun Paint.getCenteredY(): Float {
    return this.fontSpacing / 2 - this.fontMetrics.bottom
}

// 扩展获取绘制文字时在x轴上 贴紧x轴的上边缘的y坐标
fun Paint.getBottomedY(): Float {
    return -this.fontMetrics.bottom
}

// 扩展获取绘制文字时在x轴上 贴近x轴的下边缘的y坐标
fun Paint.getToppedY(): Float {
    return -this.fontMetrics.ascent
}

/* 绘制辅助工具 */

// 辅助绿幕背景

fun Canvas.helpGreenCurtain(debug: Boolean) {
    if (debug) {
        this.drawColor(Color.GREEN)
    }
}

/* 属性计算工具 */

// Flags基本操作 FlagSet是否包含Flag
fun Int.containsFlag(flag: Int): Boolean {
    return this or flag == this
}

// Flags基本操作 向FlagSet添加Flag
fun Int.addFlag(flag: Int): Int {
    return this or flag
}

// Flags基本操作 FlagSet移除Flag
fun Int.removeFlag(flag: Int): Int {
    return this and (flag.inv())
}

// 角度制转弧度制
private fun Float.degree2radian(): Float {
    return (this / 180f * PI).toFloat()
}

// 计算某角度的sin值
fun Float.degreeSin(): Float {
    return sin(this.degree2radian())
}

// 计算某角度的cos值
fun Float.degreeCos(): Float {
    return cos(this.degree2radian())
}

// 计算一个点坐标，绕原点旋转一定角度后的坐标
fun PointF.degreePointF(outPointF: PointF, degree: Float) {
    outPointF.x = this.x * degree.degreeCos() - this.y * degree.degreeSin()
    outPointF.y = this.x * degree.degreeSin() + this.y * degree.degreeCos()
}

fun Canvas.doOnCanvas(iBaseUiCanvasEvent: IBaseUiCanvasEvent) {
    save()           // 先保存画布状态
    iBaseUiCanvasEvent.doOnCanvas(this)
    restore()       // 恢复状态，确保后续绘制操作在一个干净的画布状态下进行，避免之前的绘制操作对后续的绘制操作产生影响）
}

fun isDarkMode() = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES

/**
 * Copy from Kotatsu
 */
fun hasGlobalPoint(view: View, x: Int, y: Int): Boolean {
    if (!view.isVisible) return false
    val rect = Rect()
    view.getGlobalVisibleRect(rect)
    return rect.contains(x, y)
}

fun View.measureDimension(desiredSize: Int, measureSpec: Int): Int {
    var result: Int
    val specMode = View.MeasureSpec.getMode(measureSpec)
    val specSize = View.MeasureSpec.getSize(measureSpec)
    if (specMode == View.MeasureSpec.EXACTLY) {
        result = specSize
    } else {
        result = desiredSize
        if (specMode == View.MeasureSpec.AT_MOST) {
            result = result.coerceAtMost(specSize)
        }
    }
    return result
}
