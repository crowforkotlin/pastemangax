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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    private var mHighBrushJob: Job?= null

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
     * ● 文本列表位置 -- 设置后会触发重新绘制
     *
     * ● 2023-10-31 14:06:16 周二 下午
     * @author crowforkotlin
     */
    var mListPosition : Int by Delegates.observable(0) { _, oldPosition, newPosition -> onVariableChanged(
        FLAG_REFRESH, oldPosition, newPosition, skipSameCheck = true) }

    /**
     * ● 视图对齐方式 -- 上中下
     *
     * ● 2023-10-31 15:24:43 周二 下午
     * @author crowforkotlin
     */
    var mGravity: Byte by Delegates.observable(GRAVITY_TOP_START) { _, oldSize, newSize -> onVariableChanged(
        FLAG_REFRESH, oldSize, newSize) }

    /**
     * ● 是否开启换行
     *
     * ● 2023-10-31 17:31:20 周二 下午
     * @author crowforkotlin
     */
    var mMultiLineEnable: Boolean by Delegates.observable(false) { _, oldValue, newValue -> onVariableChanged(
        FLAG_REFRESH, oldValue, newValue) }

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
     * ● UI SCOPE
     *
     * ● 2024-02-01 14:11:58 周四 下午
     * @author crowforkotlin
     */
    var mScope: CoroutineScope? = null

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
        if (mList.isEmpty()) return

        // 文本长度是否无效？
        val text = if (mListPosition !in 0..< textListSize) { mList.last() } else mList[mListPosition]

        // 执行动画
        val isDrawBrushAnimation = drawAnimation(canvas)

        // 设置X和Y的坐标 ，Paint绘制的文本在descent位置 进行相对应的计算即可
        when(mGravity) {
            GRAVITY_TOP_START -> {
                drawTopText(canvas, text, textListSize,
                    onInitializaTextY = { tryLayoutHighBrushY(it) },
                    onInitializaTextX = { tryLayoutHighBrushX(0f) }
                )
            }
            GRAVITY_TOP_CENTER -> {
                drawTopText(canvas, text, textListSize,
                    onInitializaTextY = { tryLayoutHighBrushY(it) },
                    onInitializaTextX = { tryLayoutHighBrushX((width shr 1) - it / 2f) }
                )
            }
            GRAVITY_TOP_END -> {
                drawTopText(canvas, text, textListSize,
                    onInitializaTextY = { tryLayoutHighBrushY(it) },
                    onInitializaTextX = { tryLayoutHighBrushX(width - it) }
                )
            }
            GRAVITY_CENTER_START -> {
                drawCenterText(canvas, text, textListSize,
                    onInitializaTextY = { tryLayoutHighBrushY(it) },
                    onInitializaTextX = { tryLayoutHighBrushX(0f) }
                )
            }
            GRAVITY_CENTER -> {
                drawCenterText(canvas, text, textListSize,
                    onInitializaTextY = { tryLayoutHighBrushY(it) },
                    onInitializaTextX = { tryLayoutHighBrushX((width shr 1) - it / 2f) }
                )
            }
            GRAVITY_CENTER_END -> {
                drawCenterText(canvas, text, textListSize,
                    onInitializaTextY = { tryLayoutHighBrushY(it) },
                    onInitializaTextX = { tryLayoutHighBrushX(width - it) }
                )
            }
            GRAVITY_BOTTOM_START -> {
                drawBottomText(canvas, text, textListSize,
                    onInitializaTextY = { tryLayoutHighBrushY(it) },
                    onInitializaTextX = { tryLayoutHighBrushX(0f) }
                )
            }
            GRAVITY_BOTTOM_CENTER -> {
                drawBottomText(canvas, text, textListSize,
                    onInitializaTextY = { tryLayoutHighBrushY(it) },
                    onInitializaTextX = { tryLayoutHighBrushX((width shr 1) - it /  2f) }
                )
            }
            GRAVITY_BOTTOM_END -> {
                drawBottomText(canvas, text, textListSize,
                    onInitializaTextY = { tryLayoutHighBrushY(it) },
                    onInitializaTextX = { tryLayoutHighBrushX(width - it) }
                )
            }
        }

        // 如果执行的是高刷新绘制动画并且任务正在阻塞中
        if (isDrawBrushAnimation && mHighBrushJob?.isCompleted == false) invalidateHighBrushAnimation(mHighBrushDuration )
    }

    /**
     * ● 布局高刷Y轴位置
     *
     * ● 2024-02-01 11:15:21 周四 上午
     * @author crowforkotlin
     */
    private fun tryLayoutHighBrushY(originY: Float) : Float {
        var y = originY
        if (mTextAnimationMode == ANIMATION_MOVE_Y_HIGH_BRUSH_DRAW) {
            drawView(
                onCurrent = { y +=((if(mTextAnimationTopEnable) height.toFloat() else -height.toFloat())) + mTextAxisValue },
                onNext = { y += mTextAxisValue }
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
    private fun tryLayoutHighBrushX(originX: Float)  {
        if (mTextAnimationMode == ANIMATION_MOVE_X_HIGH_BRUSH_DRAW) {
            drawView(
                onCurrent = { mTextX = (if(mTextAnimationLeftEnable) width + originX else -(width - originX)) + mTextAxisValue },
                onNext = {
                    mTextX =  originX + mTextAxisValue
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
                    val widthFloat = width.toFloat()
                    val heightFloat = height.toFloat()
                    val xRate = widthFloat * mAnimationTimeFraction
                    drawView(
                        onCurrent = { canvas.drawEraseX(widthFloat, heightFloat, xRate) },
                        onNext = { canvas.drawDifferenceEraseX(widthFloat, heightFloat, xRate) }
                    )
                }
                ANIMATION_CONTINUATION_ERASE_Y -> {
                    val widthFloat = width.toFloat()
                    val heightFloat = height.toFloat()
                    val heightRate = heightFloat * mAnimationTimeFraction
                    drawView(
                        onCurrent = { canvas.drawEraseY(widthFloat, heightFloat, heightRate) },
                        onNext =  { canvas.drawDifferenceEraseY(widthFloat, heightFloat, heightRate) }
                    )
                }
                ANIMATION_CONTINUATION_CROSS_EXTENSION -> {
                    val rectXRate = (width shr 1) * mAnimationTimeFraction
                    val rectYRate = (height shr 1) * mAnimationTimeFraction
                    val widthFloat = width.toFloat()
                    val heightFloat = height.toFloat()
                    drawView(
                        onCurrent = { canvas.drawCrossExtension(rectXRate, rectYRate, widthFloat, heightFloat) },
                        onNext = { canvas.drawDifferenceCrossExtension(rectXRate, rectYRate, widthFloat, heightFloat) }
                    )
                }
                ANIMATION_CONTINUATION_OVAL -> {
                    withPath(mPath) {
                        drawView(
                            onCurrent = {
                                canvas.drawOval(this, width, height, mAnimationTimeFraction)
                                canvas.clipPath(this)
                            },
                            onNext = {
                                canvas.drawOval(this, width, height, mAnimationTimeFraction)
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
                        canvas.drawRhombus(this, width, height, mAnimationTimeFraction)
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
                            val dx = if (mTextAnimationLeftEnable) width - mAnimationTimeFraction * width else -width + mAnimationTimeFraction * width
                            canvas.translate(dx, 0f)
                        },
                        onNext = {
                            val dx = if (mTextAnimationLeftEnable) mAnimationTimeFraction * -width else mAnimationTimeFraction * width
                            canvas.translate(dx, 0f)
                        }
                    )
                }
                ANIMATION_MOVE_Y_DRAW -> {
                    drawView(
                        onCurrent = {
                            val dy = if (mTextAnimationTopEnable) height - mAnimationTimeFraction * height else -height + mAnimationTimeFraction * height
                            canvas.translate(0f, dy)
                        },
                        onNext = {
                            val dy = if (mTextAnimationTopEnable) mAnimationTimeFraction * -height else mAnimationTimeFraction * height
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
     * ● 启动高刷绘制动画
     *
     * ● 2024-01-30 15:41:27 周二 下午
     * @author crowforkotlin
     */
    internal suspend fun launchHighBrushDrawAnimation(scope: CoroutineScope, isX: Boolean, duration: Long = IAttrText.DRAW_VIEW_MIN_DURATION) {
        mTextAxisValue = 0f
        if (isX) {
            launchHighBrushSuspendAnimation(scope, width, mTextAnimationLeftEnable, duration)
        } else {
            launchHighBrushSuspendAnimation(scope, height, mTextAnimationTopEnable, duration)
        }
    }

    /**
     * ● 高刷动画 挂起任务
     *
     * ● 2024-02-01 11:17:55 周四 上午
     * @author crowforkotlin
     */
    private suspend fun launchHighBrushSuspendAnimation(scope: CoroutineScope, count: Int, isTopOrLeft: Boolean, duration: Long) {
        mHighBrushPixelCount = count
        mHighBrushTopOrLeft = isTopOrLeft
        mHighBrushDuration = duration
        invalidateHighBrushAnimation(duration = 0)
        mHighBrushJob =  scope.launch { delay(Long.MAX_VALUE) }
        mHighBrushJob?.join()
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
                if (duration == 0L) invalidate() else mScope?.scope(duration) { invalidate() }
            } else {
                mHighBrushJob?.cancel()
            }
        } else {
            mTextAxisValue ++
            if (mTextAxisValue < mHighBrushPixelCount) {
                if (duration == 0L) invalidate() else mScope?.scope(duration) { invalidate() }
            } else {
                mHighBrushJob?.cancel()
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
            val height = height
            val heightHalf = height shr 1
            val textHeight = getTextHeight(fontMetrics)
            val textMarginRow = if (mTextRowMargin >= heightHalf) heightHalf.toFloat() else mTextRowMargin
            val textYIncremenet = textHeight + textMarginRow
            val textHeightWithMargin = textHeight + textMarginRow
            val textMaxLine =  if (height < textHeightWithMargin) 1 else (height / textHeightWithMargin).toInt()
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
        val height = height
        val heightHalf = height shr 1
        val fontMetrics = mTextPaint.fontMetrics
        val textBaseLineOffsetY = calculateBaselineOffsetY(fontMetrics)
        if (mMultiLineEnable && textListSize > 1) {
            val textHeight = getTextHeight(fontMetrics)
            var textMarginRowHalf = mTextRowMargin / 2f
            val textHeightWithMargin = textHeight + mTextRowMargin
            val textMaxRow = if (height < textHeightWithMargin) 1 else min((height / (textHeightWithMargin)).toInt(), textListSize)
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
            val height = height
            val heightHalf = height shr 1
            val textMarginRow = if (mTextRowMargin >= heightHalf) heightHalf.toFloat() else mTextRowMargin
            val textHeight = getTextHeight(mTextPaint.fontMetrics)
            val textHeightWithMargin = textHeight + textMarginRow
            val textMaxLine =  if (height < textHeightWithMargin) 1 else (measuredHeight / textHeightWithMargin).toInt()
            var textStartPos = (mListPosition + 1) * textMaxLine
            val textEndPos = mListPosition * textMaxLine
            val textYIncrement = textHeight + textMarginRow
            textStartPos = if (textListSize >= textStartPos) textStartPos - 1 else textListSize - 1
            mTextY = onInitializaTextY(height - calculateBaselineOffsetY(mTextPaint.fontMetrics))
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
            mTextY = onInitializaTextY(height - calculateBaselineOffsetY(mTextPaint.fontMetrics))
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
        canvas.drawLine(0f, (height / 2).toFloat(), width.toFloat(), (height / 2).toFloat(), paint)
        canvas.drawLine(width / 2f, 0f, width / 2f, height.toFloat(), paint)

        // 绘制底部线
        paint.color = Color.WHITE
        canvas.drawLine(0f, mTextY, width.toFloat(), mTextY, paint)

        // 绘制基线
        val ascentY = mTextY - abs(mTextPaint.fontMetrics.ascent)
        canvas.drawLine(0f, ascentY, width.toFloat(), ascentY, paint)

        // 蓝框范围
        paint.color = Color.CYAN
        paint.style = Paint.Style.STROKE
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)


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
                if (mList.isNotEmpty()) invalidate()
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
}