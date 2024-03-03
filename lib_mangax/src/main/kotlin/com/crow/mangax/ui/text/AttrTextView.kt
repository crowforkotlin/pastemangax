@file:Suppress("MemberVisibilityCanBePrivate", "KotlinConstantConditions", "unused", "SpellCheckingInspection",
    "DEPRECATION"
)

package com.crow.mangax.ui.text

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.FontMetrics
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Region
import android.os.Handler
import android.text.TextPaint
import android.view.View
import com.crow.mangax.ui.text.AttrTextLayout.Companion.ANIMATION_CONTINUATION_CROSS_EXTENSION
import com.crow.mangax.ui.text.AttrTextLayout.Companion.ANIMATION_CONTINUATION_ERASE_X
import com.crow.mangax.ui.text.AttrTextLayout.Companion.ANIMATION_CONTINUATION_ERASE_Y
import com.crow.mangax.ui.text.AttrTextLayout.Companion.ANIMATION_CONTINUATION_OVAL
import com.crow.mangax.ui.text.AttrTextLayout.Companion.ANIMATION_CONTINUATION_RHOMBUS
import com.crow.mangax.ui.text.AttrTextLayout.Companion.ANIMATION_MOVE_X_DRAW
import com.crow.mangax.ui.text.AttrTextLayout.Companion.ANIMATION_MOVE_X_HIGH_BRUSH_DRAW
import com.crow.mangax.ui.text.AttrTextLayout.Companion.ANIMATION_MOVE_Y_DRAW
import com.crow.mangax.ui.text.AttrTextLayout.Companion.ANIMATION_MOVE_Y_HIGH_BRUSH_DRAW
import com.crow.mangax.ui.text.AttrTextLayout.Companion.GRAVITY_BOTTOM_CENTER
import com.crow.mangax.ui.text.AttrTextLayout.Companion.GRAVITY_BOTTOM_END
import com.crow.mangax.ui.text.AttrTextLayout.Companion.GRAVITY_BOTTOM_START
import com.crow.mangax.ui.text.AttrTextLayout.Companion.GRAVITY_CENTER
import com.crow.mangax.ui.text.AttrTextLayout.Companion.GRAVITY_CENTER_END
import com.crow.mangax.ui.text.AttrTextLayout.Companion.GRAVITY_CENTER_START
import com.crow.mangax.ui.text.AttrTextLayout.Companion.GRAVITY_TOP_CENTER
import com.crow.mangax.ui.text.AttrTextLayout.Companion.GRAVITY_TOP_END
import com.crow.mangax.ui.text.AttrTextLayout.Companion.GRAVITY_TOP_START
import com.crow.mangax.ui.text.AttrTextLayout.Companion.STRATEGY_DIMENSION_PX_OR_DEFAULT
import com.crow.mangax.ui.text.debugText
import com.crow.mangax.ui.text.withApiO
import com.crow.mangax.ui.text.withPath
import kotlin.math.abs
import kotlin.math.min
import kotlin.properties.Delegates


/*
* 在onDraw中尽量使用inline 减少频繁调用函数带来的性能开销，需要优化计算、实现方式
* */

/**
 * ● 属性文本组件
 *
 * ● 2023/10/30 15:53
 * @author: crowforkotlin
 * @formatter:on
 */
internal class AttrTextView internal constructor(context: Context) : View(context), IAttrText {

    companion object {

        /**
         * ● 刷新标志位
         *
         * ● 2023-12-25 17:32:12 周一 下午
         * @author crowforkotlin
         */
        private const val FLAG_REFRESH: Byte = 0x01

        /**
         * ● 文本有效行默认为小于3，1奇、2偶 为3则 另外手动处理，直接给文本高度设置0 详情见 drawCenterText 函数
         *
         * ● 2023-12-25 17:29:59 周一 下午
         * @author crowforkotlin
         */
        private const val TEXT_HEIGHT_VALID_ROW: Int = 3

        /**
         * ● 用于解决文本Y轴的精准度 减少由浮点数带来的微小误差，在像素级视图中 效果十分明显
         *
         * ● 2023-12-25 17:27:58 周一 下午
         * @author crowforkotlin
         */
        private const val ROW_DEVIATION: Float = 0.5f
    }

    /**
     * ● ChildView 文本画笔
     *
     * ● 2023-11-03 15:27:29 周五 下午
     * @author crowforkotlin
     */
    lateinit var mTextPaint : TextPaint

    /**
     * ● Async Handler
     *
     * ● 2024-02-20 16:15:35 周二 下午
     * @author crowforkotlin
     */
    lateinit var mHandler: Handler

    /**
     * ● 高刷延时时间
     *
     * ● 2024-02-01 11:16:58 周四 上午
     * @author crowforkotlin
     */
    private var mHighBrushDuration = 0L

    /**
     * ● 高刷方向 是否为 Top和Left
     *
     * ● 2024-02-01 11:14:08 周四 上午
     * @author crowforkotlin
     */
    private var mHighBrushTopOrLeft = false

    /**
     * ● 高刷PX像素个数
     *
     * ● 2024-02-01 11:14:36 周四 上午
     * @author crowforkotlin
     */
    private var mHighBrushPixelCount = 0

    /**
     * ● 高刷新绘制任务
     *
     * ● 2024-01-30 15:40:31 周二 下午
     * @author crowforkotlin
     */
    private var mHighBrushJobRunning: Boolean= false

    /**
     * ● 文本X坐标
     *
     * ● 2023-10-31 14:08:08 周二 下午
     * @author crowforkotlin
     */
    private var mTextX : Float = 0f

    /**
     * ● XY轴量值
     *
     * ● 2024-01-29 17:03:09 周一 下午
     * @author crowforkotlin
     */
    private var mTextAxisValue: Float = 0f

    /**
     * ● 文本Y坐标
     *
     * ● 2023-10-31 16:18:51 周二 下午
     * @author crowforkotlin
     */
    private var mTextY : Float = 0f

    /**
     * ● Path -- 用于绘制动画
     *
     * ● 2023-12-21 19:15:44 周四 下午
     * @author crowforkotlin
     */
    var mPath = Path()

    /**
     * ● 文本列表 -- 存储屏幕上可显示的字符串集合 实现原理是 动态计算字符串宽度和 视图View做判断
     * First : 文本，Second：测量宽度
     *
     * ● 2023-10-31 14:04:26 周二 下午
     * @author crowforkotlin
     */
    var mList : MutableList<Pair<String, Float>> = mutableListOf()

    /**
     * ● 文本行数
     *
     * ● 2024-02-21 10:18:15 周三 上午
     * @author crowforkotlin
     */
    var mTextLines: Int = 1

    /**
     * ● 文本列表位置 -- 设置后会触发重新绘制
     *
     * ● 2023-10-31 14:06:16 周二 下午
     * @author crowforkotlin
     */
    var mListPosition : Int by Delegates.observable(0) { _, oldPosition, newPosition -> onVariableChanged(FLAG_REFRESH, oldPosition, newPosition, skipSameCheck = true) }

    /**
     * ● 视图对齐方式 -- 上中下
     *
     * ● 2023-10-31 15:24:43 周二 下午
     * @author crowforkotlin
     */
    var mGravity: Byte by Delegates.observable(GRAVITY_TOP_START) { _, oldSize, newSize -> onVariableChanged(FLAG_REFRESH, oldSize, newSize) }

    /**
     * ● 是否开启换行
     *
     * ● 2023-10-31 17:31:20 周二 下午
     * @author crowforkotlin
     */
    var mMultiLineEnable: Boolean by Delegates.observable(false) { _, oldValue, newValue -> onVariableChanged(FLAG_REFRESH, oldValue, newValue) }

    /**
     * ● 动画时间比率
     *
     * ● 2023-12-19 17:43:26 周二 下午
     * @author crowforkotlin
     */
    var mAnimationTimeFraction = 0f

    /**
     * ● 动画启动时间
     *
     * ● 2023-12-19 17:36:37 周二 下午
     * @author crowforkotlin
     */
    var mAnimationStartTime = 0L

    /**
     * ● 当前视图是否是显示在最前面的？
     *
     * ● 2023-12-19 19:00:24 周二 下午
     * @author crowforkotlin
     */
    var mIsCurrentView: Boolean = false

    /**
     * ● 高刷动画执行成功监听器
     *
     * ● 2024-02-18 14:57:38 周日 下午
     * @author crowforkotlin
     */
    var mHighBrushSuccessListener: Runnable? = null

    /**
     * ● 动画模式
     *
     * ● 2023-12-19 18:57:03 周二 下午
     * @author crowforkotlin
     */
    override var mTextAnimationMode: Short = 0

    /**
     * ● 动画X方向
     *
     * ● 2023-11-02 14:53:24 周四 下午
     * @author crowforkotlin
     */
    override var mTextAnimationLeftEnable: Boolean = false

    /**
     * ● 动画Y方向
     *
     * ● 2023-11-02 14:53:45 周四 下午
     * @author crowforkotlin
     */
    override var mTextAnimationTopEnable: Boolean = false

    /**
     * ● 文本的行间距
     *
     * ● 2023-12-25 15:17:16 周一 下午
     * @author crowforkotlin
     */
    override var mTextRowMargin: Float = 0f

    /**
     * ● 当前尺寸大小策略 默认PX
     *
     * ● 2023-12-26 11:37:20 周二 上午
     * @author crowforkotlin
     */
    override var mTextSizeUnitStrategy: Short = STRATEGY_DIMENSION_PX_OR_DEFAULT

    /**
     * ● 绘制文本
     *
     * ● 2023-10-31 13:33:44 周二 下午
     * @author crowforkotlin
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 文本列表长度
        val textListSize = mList.size

        // 列表为空
        if (textListSize == 0) return

        // 获取文本 -> 如果超出列表位置取最后一个
        val text = if (mListPosition !in 0..< textListSize) { mList.last() } else mList[mListPosition]

        // 执行动画
        val isDrawBrushAnimation = drawAnimation(canvas)

        // 坐标轴数值
        val textAxisValue = mTextAxisValue

        // 设置X和Y的坐标 ，Paint绘制的文本在descent位置 进行相对应的计算即可
        when(mGravity) {
            GRAVITY_TOP_START -> {
                drawTopText(canvas, text, textListSize,
                    onInitializaTextY = { tryLayoutHighBrushY(textAxisValue, it) },
                    onInitializaTextX = { tryLayoutHighBrushX(textAxisValue, 0f) }
                )
            }
            GRAVITY_TOP_CENTER -> {
                drawTopText(canvas, text, textListSize,
                    onInitializaTextY = { tryLayoutHighBrushY(textAxisValue, it) },
                    onInitializaTextX = { tryLayoutHighBrushX(textAxisValue, (measuredWidth shr 1) - it / 2f) }
                )
            }
            GRAVITY_TOP_END -> {
                drawTopText(canvas, text, textListSize,
                    onInitializaTextY = { tryLayoutHighBrushY(textAxisValue, it) },
                    onInitializaTextX = { tryLayoutHighBrushX(textAxisValue, measuredWidth - it) }
                )
            }
            GRAVITY_CENTER_START -> {
                drawCenterText(canvas, text, textListSize,
                    onInitializaTextY = { tryLayoutHighBrushY(textAxisValue, it) },
                    onInitializaTextX = { tryLayoutHighBrushX(textAxisValue, 0f) }
                )
            }
            GRAVITY_CENTER -> {
                drawCenterText(canvas, text, textListSize,
                    onInitializaTextY = { tryLayoutHighBrushY(textAxisValue, it) },
                    onInitializaTextX = { tryLayoutHighBrushX(textAxisValue, (measuredWidth shr 1) - it / 2f) }
                )
            }
            GRAVITY_CENTER_END -> {
                drawCenterText(canvas, text, textListSize,
                    onInitializaTextY = { tryLayoutHighBrushY(textAxisValue, it) },
                    onInitializaTextX = { tryLayoutHighBrushX(textAxisValue, measuredWidth - it) }
                )
            }
            GRAVITY_BOTTOM_START -> {
                drawBottomText(canvas, text, textListSize,
                    onInitializaTextY = { tryLayoutHighBrushY(textAxisValue, it) },
                    onInitializaTextX = { tryLayoutHighBrushX(textAxisValue, 0f) }
                )
            }
            GRAVITY_BOTTOM_CENTER -> {
                drawBottomText(canvas, text, textListSize,
                    onInitializaTextY = { tryLayoutHighBrushY(textAxisValue, it) },
                    onInitializaTextX = { tryLayoutHighBrushX(textAxisValue, (measuredWidth shr 1) - it /  2f) }
                )
            }
            GRAVITY_BOTTOM_END -> {
                drawBottomText(canvas, text, textListSize,
                    onInitializaTextY = { tryLayoutHighBrushY(textAxisValue, it) },
                    onInitializaTextX = { tryLayoutHighBrushX(textAxisValue, measuredWidth - it) }
                )
            }
        }

        // 如果执行的是高刷新绘制动画并且任务正在阻塞中
        if (isDrawBrushAnimation && mHighBrushJobRunning && mHighBrushDuration == 0L) invalidateHighBrushAnimation(mHighBrushDuration )
    }

    /**
     * ● 布局高刷Y轴位置
     *
     * ● 2024-02-01 11:15:21 周四 上午
     * @author crowforkotlin
     */
    private fun tryLayoutHighBrushY(textAxisValue: Float, originY: Float) : Float {
        var y = originY
        if (mTextAnimationMode == ANIMATION_MOVE_Y_HIGH_BRUSH_DRAW) {
            drawView(
                onCurrent = { y +=((if(mTextAnimationTopEnable) measuredHeight.toFloat() else -measuredHeight.toFloat())) + textAxisValue },
                onNext = { y += textAxisValue }
            )
        }
        return y
    }

    /**
     * ● 布局高刷X轴位置
     *
     * ● 2024-02-01 11:15:40 周四 上午
     * @author crowforkotlin
     */
    private fun tryLayoutHighBrushX(textAxisValue: Float, originX: Float)  {
        if (mTextAnimationMode == ANIMATION_MOVE_X_HIGH_BRUSH_DRAW) {
            drawView(
                onCurrent = { mTextX = (if(mTextAnimationLeftEnable) measuredWidth + originX else -(measuredWidth - originX)) + textAxisValue },
                onNext = {
                    mTextX =  originX + textAxisValue
                }
            )
        }
        else mTextX = originX
    }

    /**
     * ● 绘制Canvas动画
     *
     * ● 2023-12-22 15:21:59 周五 下午
     * @author crowforkotlin
     */
    private fun drawAnimation(canvas: Canvas) : Boolean{
        if (mAnimationStartTime > 0) {
            when(mTextAnimationMode) {
                ANIMATION_CONTINUATION_ERASE_X -> {
                    val widthFloat = measuredWidth.toFloat()
                    val heightFloat = measuredHeight.toFloat()
                    val xRate = widthFloat * mAnimationTimeFraction
                    drawView(
                        onCurrent = { canvas.drawEraseX(widthFloat, heightFloat, xRate) },
                        onNext = { canvas.drawDifferenceEraseX(widthFloat, heightFloat, xRate) }
                    )
                }
                ANIMATION_CONTINUATION_ERASE_Y -> {
                    val widthFloat = measuredWidth.toFloat()
                    val heightFloat = measuredHeight.toFloat()
                    val heightRate = heightFloat * mAnimationTimeFraction
                    drawView(
                        onCurrent = { canvas.drawEraseY(widthFloat, heightFloat, heightRate) },
                        onNext =  { canvas.drawDifferenceEraseY(widthFloat, heightFloat, heightRate) }
                    )
                }
                ANIMATION_CONTINUATION_CROSS_EXTENSION -> {
                    val rectXRate = (measuredWidth shr 1) * mAnimationTimeFraction
                    val rectYRate = (measuredHeight shr 1) * mAnimationTimeFraction
                    val widthFloat = measuredWidth.toFloat()
                    val heightFloat = measuredHeight.toFloat()
                    drawView(
                        onCurrent = { canvas.drawCrossExtension(rectXRate, rectYRate, widthFloat, heightFloat) },
                        onNext = { canvas.drawDifferenceCrossExtension(rectXRate, rectYRate, widthFloat, heightFloat) }
                    )
                }
                ANIMATION_CONTINUATION_OVAL -> {
                    withPath(mPath) {
                        drawView(
                            onCurrent = {
                                canvas.drawOval(this, measuredWidth, measuredHeight, mAnimationTimeFraction)
                                canvas.clipPath(this)
                            },
                            onNext = {
                                canvas.drawOval(this, measuredWidth, measuredHeight, mAnimationTimeFraction)
                                withApiO(
                                    leastO = { canvas.clipOutPath(this) },
                                    lessO = { canvas.clipPath(this, Region.Op.DIFFERENCE) }
                                )
                            }
                        )
                    }
                }
                ANIMATION_CONTINUATION_RHOMBUS -> {
                    withPath(mPath) {
                        canvas.drawRhombus(this, measuredWidth, measuredHeight, mAnimationTimeFraction)
                        drawView(
                            onCurrent = {
                                withApiO(
                                    leastO = { canvas.clipOutPath(this) },
                                    lessO = { canvas.clipPath(this, Region.Op.XOR) }
                                )
                            },
                            onNext = {
                                withApiO(
                                    leastO = { canvas.clipPath(this) },
                                    lessO = { canvas.clipPath(this, Region.Op.REPLACE) }
                                )
                            }
                        )
                    }
                }
                ANIMATION_MOVE_X_DRAW -> {
                    drawView(
                        onCurrent = {
                            val dx = if (mTextAnimationLeftEnable) measuredWidth - mAnimationTimeFraction * measuredWidth else -measuredWidth + mAnimationTimeFraction * measuredWidth
                            canvas.translate(dx, 0f)
                        },
                        onNext = {
                            val dx = if (mTextAnimationLeftEnable) mAnimationTimeFraction * -measuredWidth else mAnimationTimeFraction * measuredWidth
                            canvas.translate(dx, 0f)
                        }
                    )
                }
                ANIMATION_MOVE_Y_DRAW -> {
                    drawView(
                        onCurrent = {
                            val dy = if (mTextAnimationTopEnable) measuredHeight - mAnimationTimeFraction * measuredHeight else -measuredHeight + mAnimationTimeFraction * measuredHeight
                            canvas.translate(0f, dy)
                        },
                        onNext = {
                            val dy = if (mTextAnimationTopEnable) mAnimationTimeFraction * -measuredHeight else mAnimationTimeFraction * measuredHeight
                            canvas.translate(0f, dy)
                        }
                    )
                }
                ANIMATION_MOVE_X_HIGH_BRUSH_DRAW, ANIMATION_MOVE_Y_HIGH_BRUSH_DRAW -> return true
            }
        }
        return false
    }

    /**
     * ● 高刷动画 挂起任务
     *
     * ● 2024-02-01 11:17:55 周四 上午
     * @author crowforkotlin
     */
    private fun launchHighBrushSuspendAnimation(count: Int, isTopOrLeft: Boolean, duration: Long) {
        mHighBrushPixelCount = count
        mHighBrushTopOrLeft = isTopOrLeft
        mHighBrushDuration = duration
        invalidateHighBrushAnimation(duration = duration)
    }

    /**
     * ● 更新高刷动画
     *
     * ● 2024-02-01 14:15:20 周四 下午
     * @author crowforkotlin
     */
    private fun invalidateHighBrushAnimation(duration: Long) {
        if (mHighBrushTopOrLeft) {
            mTextAxisValue --
            if (mTextAxisValue > -mHighBrushPixelCount) {
                if (duration == 0L) invalidate() else {
                    mHandler.post(object : Runnable {
                        override fun run() {
                            if (mTextAxisValue > -mHighBrushPixelCount) invalidate()
                            else {
                                mHandler.removeCallbacks(this)
                                mHighBrushJobRunning = false
                                mHighBrushSuccessListener?.run()
                                return
                            }
                            mTextAxisValue--
                            mHandler.postDelayed(this, duration)
                        }
                    })
                }
            } else {
                mHighBrushJobRunning = false
                mHighBrushSuccessListener?.run()
            }
        } else {
            mTextAxisValue ++
            if (mTextAxisValue < mHighBrushPixelCount) {
                if (duration == 0L) invalidate() else {
                    mHandler.post(object : Runnable {
                        override fun run() {
                            if (mTextAxisValue < mHighBrushPixelCount) invalidate()
                            else {
                                mHandler.removeCallbacks(this)
                                mHighBrushJobRunning = false
                                mHighBrushSuccessListener?.run()
                                return
                            }
                            mTextAxisValue ++
                            mHandler.postDelayed(this, duration)
                        }
                    })
                }
            } else {
                mHighBrushJobRunning = false
                mHighBrushSuccessListener?.run()
            }
        }
    }

    /**
     * ● 绘制顶部文本
     *
     * ● 2023-11-04 17:53:43 周六 下午
     * @author crowforkotlin
     */
    private inline fun drawTopText(canvas: Canvas, text: Pair<String, Float>, textListSize: Int, onInitializaTextY: (Float) -> Float, onInitializaTextX: (Float) -> Unit) {
        val fontMetrics = mTextPaint.fontMetrics
        if (mMultiLineEnable && textListSize > 1) {
            val measuredHeight = measuredHeight
            val heightHalf = measuredHeight shr 1
            val textHeight = getTextHeight(fontMetrics)
            val textMarginRow = if (mTextRowMargin >= heightHalf) heightHalf.toFloat() else mTextRowMargin
            val textYIncremenet = textHeight + textMarginRow
            val textHeightWithMargin = textHeight + textMarginRow
            val textMaxLine = min(if (measuredHeight < textHeightWithMargin) 1 else (measuredHeight / textHeightWithMargin).toInt(), mTextLines)
            var textStartPos = mListPosition * textMaxLine
            mTextY = onInitializaTextY(abs(fontMetrics.ascent))
            repeat(textMaxLine) {
                if (textStartPos < mList.size) {
                    val currentText = mList[textStartPos]
                    onInitializaTextX(currentText.second)
                    canvas.drawText(currentText.first)
                    textStartPos ++
                    mTextY += textYIncremenet
                } else return
            }
        } else {
            mTextY = onInitializaTextY(abs(fontMetrics.ascent))
            onInitializaTextX(text.second)
            canvas.drawText(text.first)
        }
    }

    /**
     * ● 绘制中心文本
     *
     * ● 2023-11-04 17:54:09 周六 下午
     * @author crowforkotlin
     */
    private inline fun drawCenterText(canvas: Canvas, text: Pair<String, Float>, textListSize: Int, onInitializaTextY: (Float) -> Float, onInitializaTextX: (Float) -> Unit) {
        val measuredHeight = measuredHeight
        val heightHalf = measuredHeight shr 1
        val fontMetrics = mTextPaint.fontMetrics
        val textBaseLineOffsetY = calculateBaselineOffsetY(fontMetrics)
        if (mMultiLineEnable && textListSize > 1) {
            val textHeight = getTextHeight(fontMetrics)
            var textMarginRowHalf = mTextRowMargin / 2f
            val textHeightWithMargin = textHeight + mTextRowMargin
            val textMaxRow = if (measuredHeight < textHeightWithMargin) 1 else min((measuredHeight / (textHeightWithMargin)).toInt(), textListSize)
            var textStartPos = (mListPosition * textMaxRow).let { if (it >= textListSize) it - textMaxRow else it }
            val textValidRow = if (textStartPos + textMaxRow <= textListSize) textMaxRow else textListSize - textStartPos
            val textValidRowHalf = textValidRow shr 1
            if (textMaxRow == 1 || textValidRow == 1) textMarginRowHalf = 0f
            mTextY = onInitializaTextY(
                if (textValidRow % 2 == 0) { // 考虑到 偶数、奇数 行居中的效果
                    (heightHalf - (textHeightWithMargin * if(textValidRow < TEXT_HEIGHT_VALID_ROW) 0 else textValidRowHalf - 1)) - textBaseLineOffsetY - textMarginRowHalf + ROW_DEVIATION
                } else {
                    (heightHalf - (textHeightWithMargin * if(textValidRow < TEXT_HEIGHT_VALID_ROW) 0 else textValidRowHalf)) + textBaseLineOffsetY - ROW_DEVIATION
                }
            )
            repeat(textValidRow) {
                if (textStartPos < textListSize) {
                    val currentText: Pair<String, Float> = mList[textStartPos]
                    onInitializaTextX(currentText.second)
                    canvas.drawText(currentText.first)
                    textStartPos ++
                    mTextY += textHeightWithMargin
                } else return
            }
        } else {
            mTextY = onInitializaTextY(heightHalf + textBaseLineOffsetY)
            onInitializaTextX(text.second)
            canvas.drawText(text.first)
        }
    }

    /**
     * ● 绘制底部文本
     *
     * ● 2023-11-04 17:54:00 周六 下午
     * @author crowforkotlin
     */
    private inline fun drawBottomText(canvas: Canvas, text: Pair<String, Float>, textListSize: Int, onInitializaTextY: (Float) -> Float, onInitializaTextX: (Float) -> Unit) {
        if (mMultiLineEnable && textListSize > 1) {
            val measuredHeight = measuredHeight
            val heightHalf = measuredHeight shr 1
            val textMarginRow = if (mTextRowMargin >= heightHalf) heightHalf.toFloat() else mTextRowMargin
            val textHeight = getTextHeight(mTextPaint.fontMetrics)
            val textHeightWithMargin = textHeight + textMarginRow
            val textMaxLine =  if (measuredHeight < textHeightWithMargin) 1 else (measuredHeight / textHeightWithMargin).toInt()
            var textStartPos = (mListPosition + 1) * textMaxLine
            val textEndPos = mListPosition * textMaxLine
            val textYIncrement = textHeight + textMarginRow
            textStartPos = if (textListSize >= textStartPos) textStartPos - 1 else textListSize - 1
            mTextY = onInitializaTextY(measuredHeight - calculateBaselineOffsetY(mTextPaint.fontMetrics))
            repeat(textMaxLine) {
                if (textStartPos >= textEndPos) {
                    val currentText: Pair<String, Float> = mList[textStartPos]
                    onInitializaTextX(currentText.second)
                    canvas.drawText(currentText.first)
                    textStartPos --
                    mTextY -= textYIncrement
                } else return
            }
        } else {
            mTextY = onInitializaTextY(measuredHeight - calculateBaselineOffsetY(mTextPaint.fontMetrics))
            onInitializaTextX(text.second)
            canvas.drawText(text.first)
        }
    }

    /**
     * ● Debug Logic Function
     *
     * ● 2023-11-07 18:44:26 周二 下午
     * @author crowforkotlin
     */
    private fun drawDebugTextLine(canvas: Canvas) {

        // 绘制中线
        val paint = TextPaint()
        paint.color = Color.GREEN
        paint.strokeWidth = IAttrText.DEBUG_STROKE_WIDTH
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
        canvas.drawLine(0f, (measuredHeight / 2).toFloat(), measuredWidth.toFloat(), (measuredHeight / 2).toFloat(), paint)
        canvas.drawLine(measuredWidth / 2f, 0f, measuredWidth / 2f, measuredHeight.toFloat(), paint)

        // 绘制底部线
        paint.color = Color.WHITE
        canvas.drawLine(0f, mTextY, measuredWidth.toFloat(), mTextY, paint)

        // 绘制基线
        val ascentY = mTextY - abs(mTextPaint.fontMetrics.ascent)
        canvas.drawLine(0f, ascentY, measuredWidth.toFloat(), ascentY, paint)

        // 蓝框范围
        paint.color = Color.CYAN
        paint.style = Paint.Style.STROKE
        canvas.drawRect(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), paint)


        mTextPaint.color = Color.RED
    }

    /**
     * ● 计算 baseline 的相对文字中心的偏移量
     *
     * ● 2023-10-31 13:34:50 周二 下午
     * @author crowforkotlin
     */
    private fun calculateBaselineOffsetY(fontMetrics: FontMetrics): Float {
        return -fontMetrics.ascent / 2f - fontMetrics.descent / 2f
    }

    /**
     * ● 值发生变化 执行对于的Logic
     *
     * ● 2023-10-31 14:14:18 周二 下午
     * @author crowforkotlin
     */
    private fun<T : Any> onVariableChanged(flag: Byte, oldValue: T?, newValue: T?, skipSameCheck: Boolean = false) {
        if (oldValue == newValue && !skipSameCheck) return
        when(flag) {
            FLAG_REFRESH -> {
                if (mList.isNotEmpty()) mHandler.post { invalidate() }
            }
        }
    }

    /**
     * ● 根据FLAG绘制视图
     *
     * ● 2023-12-22 19:05:36 周五 下午
     * @author crowforkotlin
     */
    private inline fun drawView(onCurrent: () -> Unit, onNext: () -> Unit) { if (mIsCurrentView) onCurrent() else onNext() }

    /**
     * ● 绘制文本
     *
     * ● 2023-12-22 19:05:29 周五 下午
     * @author crowforkotlin
     */
    private fun Canvas.drawText(text: String) {
        debugText(
            onDebug = {
                drawText(text, 0, text.length, mTextX, mTextY, mTextPaint)
                drawDebugTextLine(this)
            },
            orElse = {
                drawText(text, 0, text.length, mTextX, mTextY, mTextPaint)
            }
        )
    }

    /**
     * ● 启动高刷绘制动画
     *
     * ● 2024-01-30 15:41:27 周二 下午
     * @author crowforkotlin
     */
    internal fun launchHighBrushDrawAnimation(isX: Boolean, duration: Long = IAttrText.DRAW_VIEW_MIN_DURATION) {
        mTextAxisValue = 0f
        mHighBrushJobRunning = true
        if (isX) {
            launchHighBrushSuspendAnimation(measuredWidth, mTextAnimationLeftEnable, duration)
        } else {
            launchHighBrushSuspendAnimation(measuredHeight, mTextAnimationTopEnable, duration)
        }
    }
    internal fun setHighBrushSuccessListener(listener: Runnable) { mHighBrushSuccessListener = listener }
}
