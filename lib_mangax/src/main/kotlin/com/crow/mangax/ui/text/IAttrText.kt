@file:Suppress("SpellCheckingInspection", "unused", "DEPRECATION", "LocalVariableName", "AnnotateVersionCheck")

package com.crow.mangax.ui.text

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Region
import android.util.Log
import kotlin.math.sqrt

internal interface IAttrText {

    companion object {

        /**
         * ● 调试模式
         *
         * ● 2023-12-22 15:13:15 周五 下午
         * @author crowforkotlin
         */
        internal const val DEBUG = false
        internal const val DEBUG_TEXT = false
        internal const val DEBUG_ANIMATION = false

        internal const val TAG = "IAttrTextExt-Crow"
        internal const val DEBUG_STROKE_WIDTH = 8f
        internal val mDebugYelloPaint by lazy { Paint().also { it.strokeWidth = DEBUG_STROKE_WIDTH; it.color = Color.YELLOW } }
        internal val mDebugBluePaint by lazy { Paint().also { it.strokeWidth = DEBUG_STROKE_WIDTH; it.color = Color.BLUE } }

        /**
         * ● 绘制时间16MS
         *
         * ● 2024-01-30 15:43:47 周二 下午
         * @author crowforkotlin
         */
        internal const val DRAW_VIEW_MIN_DURATION = 16L
    }

    var mTextAnimationTopEnable: Boolean
    var mTextAnimationLeftEnable: Boolean
    var mTextAnimationMode: Short
    var mTextRowMargin: Float
    var mTextSizeUnitStrategy: Short

    /**
     * ● 获取文本高度：ascent绝对值 + descent
     *
     * ● 2023-11-29 17:01:15 周三 下午
     * @author crowforkotlin
     */
    fun getTextHeight(fontMetrics: Paint.FontMetrics) : Float {
        return fontMetrics.descent -fontMetrics.ascent
    }

    /**
     * ● 绘制菱形
     *
     * ● 2023-12-25 15:19:02 周一 下午
     * @author crowforkotlin
     */
    fun Canvas.drawRhombus(path: Path, width: Int, height: Int, fraction: Float) {
        val halfWidth = width shr 1
        val halfHeight = height shr 1
        val halfWidthFloat = halfWidth.toFloat()
        val halfHeightFloat = halfHeight.toFloat()
        val xRate = width * fraction
        val yRate = height * fraction
        path.moveTo(halfWidthFloat, -halfHeight + yRate)
        path.lineTo(-halfWidth + xRate, halfHeightFloat)
        path.lineTo(halfWidthFloat, height + halfHeight - yRate)
        path.lineTo(width + halfWidth - xRate, halfHeightFloat)
        debugAnimation {
            drawLine(halfWidthFloat, -halfHeight + yRate, halfWidthFloat + halfWidthFloat, (-halfHeight + yRate) + (-halfHeight + yRate), mDebugYelloPaint)
            drawLine(-halfWidth + xRate, halfHeightFloat, (-halfWidth + xRate) + (-halfWidth + xRate), halfHeightFloat + halfHeightFloat, mDebugYelloPaint)
            drawLine(halfWidthFloat, height + halfHeight - yRate, halfWidthFloat + halfWidthFloat, (height + halfHeight - yRate) + (height + halfHeight - yRate), mDebugYelloPaint)
            drawLine(width + halfWidth - xRate, halfHeightFloat, (width + halfWidth - xRate) + (width + halfWidth - xRate), halfHeightFloat + halfHeightFloat, mDebugYelloPaint)
        }
    }

    /**
     * ● 绘制圆形 时钟动画
     *
     * ● 2023-12-25 15:22:48 周一 下午
     * @author crowforkotlin
     */
    fun Canvas.drawOval(path: Path, width: Int, height: Int, fraction: Float) {
        val widthFloat = width.toFloat()
        val heightFloat = height.toFloat()
        val diagonal = sqrt(widthFloat * widthFloat + heightFloat * heightFloat)
        val widthHalf = widthFloat / 2f
        val heightHalf = heightFloat / 2f
        path.addArc(widthHalf - diagonal, heightHalf - diagonal, widthFloat + diagonal - widthHalf, heightFloat + diagonal -heightHalf,270f,360 * fraction)
        path.lineTo(widthHalf,heightHalf)
        debugAnimation {
            drawLine(widthHalf - diagonal, heightHalf - diagonal, width + diagonal - widthHalf, height + diagonal - heightHalf, mDebugBluePaint)
            drawLine(0f, 0f, widthHalf, heightHalf, mDebugBluePaint)
        }
    }

    /**
     * ● 绘制十字扩展 动画
     *
     * ● 2023-12-25 15:23:15 周一 下午
     * @author crowforkotlin
     */
    fun Canvas.drawCrossExtension(width: Int, height: Int, fraction: Float) {
        val rectXRate = (width shr 1) * fraction
        val rectYRate = (height shr 1) * fraction
        val widthFloat = width.toFloat()
        val heightFloat = height.toFloat()
        drawCrossExtension(rectXRate, rectYRate, widthFloat, heightFloat)
    }

    /**
     * ● 绘制十字扩展 动画
     *
     * ● 2023-12-25 15:23:15 周一 下午
     * @author crowforkotlin
     */
    fun Canvas.drawCrossExtension(rectXRate: Float, rectYRate: Float, widthFloat: Float, heightFloat: Float) {
        withApiO(
            leastO = {
                clipOutRect(0f,  rectYRate, widthFloat, heightFloat - rectYRate) // 上下
                clipOutRect(rectXRate, 0f, widthFloat - rectXRate, heightFloat)  // 左右
                debugAnimation {
                    drawLine(0f,  rectYRate, widthFloat, heightFloat - rectYRate, mDebugBluePaint) // 上下
                    drawLine(rectXRate, 0f, widthFloat - rectXRate, heightFloat, mDebugBluePaint)  // 左右
                }
            },
            lessO = {
                clipRect(0f,  rectYRate, widthFloat, heightFloat - rectYRate, Region.Op.DIFFERENCE) // 上下
                clipRect(rectXRate, 0f, widthFloat - rectXRate, heightFloat, Region.Op.DIFFERENCE)  // 左右
                debugAnimation {
                    drawLine(0f,  rectYRate, widthFloat, heightFloat - rectYRate, mDebugBluePaint) // 上下
                    drawLine(rectXRate, 0f, widthFloat - rectXRate, heightFloat, mDebugBluePaint)  // 左右
                }
            }
        )
    }

    /**
     * ● 绘制同方向 反效果的十字扩展 动画
     *
     * ● 2023-12-25 15:23:52 周一 下午
     * @author crowforkotlin
     */
    fun Canvas.drawDifferenceCrossExtension(rectXRate: Float, rectYRate: Float, widthFloat: Float, heightFloat: Float) {
        clipRect(0f,  rectYRate, widthFloat, height - rectYRate) // 上下
        clipRect(rectXRate, 0f, width - rectXRate, height.toFloat())  // 左右
        debugAnimation {
            drawLine(0f,  rectYRate, widthFloat, height - rectYRate, mDebugYelloPaint)
            drawLine(rectXRate, 0f, width - rectXRate, height.toFloat(), mDebugYelloPaint)
        }
    }

    /**
     * ● 绘制擦除Y轴方向的动画
     *
     * ● 2023-12-25 15:24:31 周一 下午
     * @author crowforkotlin
     */
    fun Canvas.drawEraseY(widthFloat: Float, heightFloat: Float, yRate: Float) {
        drawY(
            onTop = {
                clipRect(0f, heightFloat - yRate, widthFloat, heightFloat)
                debugAnimation { drawLine(0f, heightFloat - yRate, widthFloat, heightFloat, mDebugBluePaint) }
            },
            onBottom = {
                clipRect(0f, 0f, widthFloat, yRate)
                debugAnimation { drawLine(0f, 0f, widthFloat, yRate, mDebugYelloPaint) }
            }
        )
    }

    /**
     * ● 绘制同方向 反效果的擦除Y轴方向的动画
     *
     * ● 2023-12-25 15:24:43 周一 下午
     * @author crowforkotlin
     */
    fun Canvas.drawDifferenceEraseY(widthFloat: Float, heightFloat: Float, yRate: Float) {
        drawY(
            onTop = {
                clipRect(0f, 0f, widthFloat, heightFloat - yRate)
                debugAnimation { drawLine(0f, 0f, widthFloat, heightFloat - yRate, mDebugYelloPaint) }
            },
            onBottom = {
                clipRect(0f, yRate, widthFloat, heightFloat)
                debugAnimation { drawLine(0f, yRate, widthFloat, heightFloat, mDebugBluePaint) }
            }
        )
    }

    /**
     * ● 绘制擦除X轴方向的动画
     *
     * ● 2023-12-25 15:25:01 周一 下午
     * @author crowforkotlin
     */
    fun Canvas.drawEraseX(widthFloat: Float, heightFloat: Float, xRate: Float) {
        drawX(
            onLeft = {
                clipRect(widthFloat - xRate, 0f, widthFloat, heightFloat)
                debugAnimation {
                    drawLine(widthFloat - xRate, 0f, widthFloat, heightFloat, mDebugBluePaint)
                }
            },
            onRight = {
                debugAnimation {
                    drawLine(0f, 0f, xRate, heightFloat, mDebugYelloPaint)
                }
                clipRect(0f, 0f, xRate, heightFloat)
            }
        )
    }

    /**
     * ● 绘制同方向 反效果的擦除X轴方向的动画
     *
     * ● 2023-12-25 15:25:48 周一 下午
     * @author crowforkotlin
     */
    fun Canvas.drawDifferenceEraseX(widthFloat: Float, heightFloat: Float, xRate: Float) {
        drawX(
            onLeft = {
                clipRect(0f, 0f, widthFloat - xRate, heightFloat)
                debugAnimation {
                    drawLine(0f, 0f, widthFloat - xRate, heightFloat, mDebugYelloPaint)
                }
            },
            onRight = {
                clipRect(xRate, 0f, widthFloat, heightFloat)
                debugAnimation {
                    drawLine(xRate, 0f, widthFloat, heightFloat, mDebugBluePaint)
                }
            }
        )
    }

    /**
     * ● 调试模式打印
     *
     * ● 2023-12-25 16:39:20 周一 下午
     * @author crowforkotlin
     */
    fun Any?.debugLog(tag: String = TAG, level: Int = Log.DEBUG) {
        debug { Log.println(level, tag, this.toString()) }
    }

    /**
     * ● DP转PX
     *
     * ● 2023-12-28 10:44:00 周四 上午
     * @author crowforkotlin
     */
    fun Context.px2dp(dp: Float): Float {
        return dp * resources.displayMetrics.density + 0.5f
    }

    /**
     * ● PX转SP
     *
     * ● 2023-12-28 10:44:26 周四 上午
     * @author crowforkotlin
     */
    fun Context.px2sp(px: Float): Float {
        return px * resources.displayMetrics.density + 0.5f
    }
}