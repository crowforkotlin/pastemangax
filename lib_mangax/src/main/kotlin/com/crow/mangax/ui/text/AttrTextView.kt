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
import com.crow.mangax.ui.text.AttrTextLayout.Companion.GRAVITY_BOTTOM_CENTER
import com.crow.mangax.ui.text.AttrTextLayout.Companion.GRAVITY_BOTTOM_END
import com.crow.mangax.ui.text.AttrTextLayout.Companion.GRAVITY_BOTTOM_START
import com.crow.mangax.ui.text.AttrTextLayout.Companion.GRAVITY_CENTER
import com.crow.mangax.ui.text.AttrTextLayout.Companion.GRAVITY_CENTER_END
import com.crow.mangax.ui.text.AttrTextLayout.Companion.GRAVITY_CENTER_START
import com.crow.mangax.ui.text.AttrTextLayout.Companion.GRAVITY_TOP_CENTER
import com.crow.mangax.ui.text.AttrTextLayout.Companion.GRAVITY_TOP_END
import com.crow.mangax.ui.text.AttrTextLayout.Companion.GRAVITY_TOP_START
import com.crow.mangax.ui.text.AttrTextLayout.Companion.STRATEGY_DIMENSION_PX
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
class AttrTextView(context: Context) : View(context), IAttrText {

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
     * ● 文本X坐标
     *
     * ● 2023-10-31 14:08:08 周二 下午
     * @author crowforkotlin
     */
    private var mTextX : Float = 0f

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
     * ● 动画模式
     *
     * ● 2023-12-19 18:57:03 周二 下午
     * @author crowforkotlin
     */
    override var mAnimationMode: Short = 0


    /**
     * ● 动画X方向
     *
     * ● 2023-11-02 14:53:24 周四 下午
     * @author crowforkotlin
     */
    override var mAnimationLeft: Boolean = false

    /**
     * ● 动画Y方向
     *
     * ● 2023-11-02 14:53:45 周四 下午
     * @author crowforkotlin
     */
    override var mAnimationTop: Boolean = false

    /**
     * ● 文本的行间距
     *
     * ● 2023-12-25 15:17:16 周一 下午
     * @author crowforkotlin
     */
    override var mMarginRow: Float = 0f

    /**
     * ● 当前尺寸大小策略 默认PX
     *
     * ● 2023-12-26 11:37:20 周二 上午
     * @author crowforkotlin
     */
    override var mSizeUnitStrategy: Short = STRATEGY_DIMENSION_PX

    /**
     * ● 设置硬件加速渲染
     *
     * ● 2023-11-10 15:16:42 周五 下午
     * @author crowforkotlin
     */
    init {

        // 设置View使用硬件加速渲染绘制， 不然Animation移动View会造成绘制的内容抖动
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    /**
     * ● 绘制文本
     *
     * ● 2023-10-31 13:33:44 周二 下午
     * @author crowforkotlin
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 执行动画
        drawAnimation(canvas)

        // 文本列表长度
        val textListSize = mList.size

        // 文本长度是否无效？
        val isLengthInvalid = mListPosition > textListSize - 1

        // 画笔未初始化 长度是否无效 列表位置是否小于0
        if (!::mTextPaint.isInitialized || isLengthInvalid || mListPosition < 0) return

        // 获取文本
        val text = if (isLengthInvalid) { mList.last() } else mList[mListPosition]

        // 设置X和Y的坐标 ，Paint绘制的文本在descent位置 进行相对应的计算即可
        when(mGravity) {
            GRAVITY_TOP_START -> {
                mTextY = abs(mTextPaint.fontMetrics.ascent)
                drawTopText(canvas, text, textListSize) { mTextX = 0f }
            }
            GRAVITY_TOP_CENTER -> {
                mTextY = abs(mTextPaint.fontMetrics.ascent)
                drawTopText(canvas, text, textListSize) { mTextX = (width shr 1) - it / 2f }
            }
            GRAVITY_TOP_END -> {
                mTextY = abs(mTextPaint.fontMetrics.ascent)
                drawTopText(canvas, text, textListSize) { mTextX = width - it }
            }
            GRAVITY_CENTER_START -> {
                drawCenterText(canvas, text, textListSize) { mTextX = 0f }
            }
            GRAVITY_CENTER -> {
                drawCenterText(canvas, text, textListSize) { mTextX = (width shr 1) - it / 2f }
            }
            GRAVITY_CENTER_END -> {
                drawCenterText(canvas, text, textListSize) { mTextX = width - it }
            }
            GRAVITY_BOTTOM_START -> {
                mTextY = height - calculateBaselineOffsetY(mTextPaint.fontMetrics)
                drawBottomText(canvas, text, textListSize) { mTextX = 0f }
            }
            GRAVITY_BOTTOM_CENTER -> {
                mTextY = height - calculateBaselineOffsetY(mTextPaint.fontMetrics)
                drawBottomText(canvas, text, textListSize) { mTextX =  (width shr 1) - it /  2f }
            }
            GRAVITY_BOTTOM_END -> {
                mTextY = height - calculateBaselineOffsetY(mTextPaint.fontMetrics)
                drawBottomText(canvas, text, textListSize) { mTextX = width - it }
            }
        }
    }

    /**
     * ● 绘制Canvas动画
     *
     * ● 2023-12-22 15:21:59 周五 下午
     * @author crowforkotlin
     */
    private fun drawAnimation(canvas: Canvas) {
        if (mAnimationStartTime > 0) {
            when(mAnimationMode) {
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
            }
        }
    }

    /**
     * ● 绘制顶部文本
     *
     * ● 2023-11-04 17:53:43 周六 下午
     * @author crowforkotlin
     */
    private inline fun drawTopText(canvas: Canvas, text: Pair<String, Float>, textListSize: Int, onIniTextX: (Float) -> Unit) {
        if (mMultiLineEnable && textListSize > 1) {
            val heightHalf = height shr 1
            val textHeight = getTextHeight(mTextPaint.fontMetrics)
            val marginRow = if (mMarginRow >= heightHalf) heightHalf.toFloat() else mMarginRow
            val textHeightWithMargin = textHeight + marginRow
            val maxLine =  if (height < textHeightWithMargin) 1 else (measuredHeight / (textHeightWithMargin)).toInt()
            var listStartPos = mListPosition * maxLine
            val textYIncremenet = textHeight + marginRow
            repeat(maxLine) {
                if (listStartPos < mList.size) {
                    val currentText = mList[listStartPos]
                    onIniTextX(currentText.second)
                    canvas.drawText(currentText.first)
                    listStartPos ++
                    mTextY += textYIncremenet
                } else return
            }
        } else {
            onIniTextX(text.second)
            canvas.drawText(text.first)
        }
    }

    /**
     * ● 绘制中心文本
     *
     * ● 2023-11-04 17:54:09 周六 下午
     * @author crowforkotlin
     */
    private inline fun drawCenterText(canvas: Canvas, text: Pair<String, Float>, textListSize: Int, onInitializaTextX: (Float) -> Unit) {
        val screenHeightHalf = height shr 1
        val fontMetrics = mTextPaint.fontMetrics
        val textHeight = getTextHeight(fontMetrics)
        val baseLineOffsetY = calculateBaselineOffsetY(fontMetrics)
        var halfMarginRow = mMarginRow / 2f
        if (mMultiLineEnable && textListSize > 1) {
            val textHeightWithMargin = textHeight + mMarginRow
            val maxRow = if (height < textHeightWithMargin) 1 else min((height / (textHeightWithMargin)).toInt(), textListSize)
            var listStartPos = (mListPosition * maxRow).let { if (it >= textListSize) it - maxRow else it }
            val validRow = if (listStartPos + maxRow <= textListSize) maxRow else textListSize - listStartPos
            val halfCount = validRow shr 1
            if (maxRow == 1 || validRow == 1) halfMarginRow = 0f
            mTextY = if (validRow % 2 == 0) { // 考虑到 偶数、奇数 行居中的效果
                (screenHeightHalf - (textHeightWithMargin * if(validRow < TEXT_HEIGHT_VALID_ROW) 0 else halfCount - 1)) - baseLineOffsetY - halfMarginRow + ROW_DEVIATION
            } else {
                (screenHeightHalf - (textHeightWithMargin * if(validRow < TEXT_HEIGHT_VALID_ROW) 0 else halfCount)) + baseLineOffsetY - ROW_DEVIATION
            }
            repeat(validRow) {
                if (listStartPos < textListSize) {
                    val currentText = mList[listStartPos]
                    onInitializaTextX(currentText.second)
                    canvas.drawText(currentText.first)
                    listStartPos ++
                    mTextY += textHeightWithMargin
                } else return
            }
        } else {
            onInitializaTextX(text.second)
            mTextY = screenHeightHalf + baseLineOffsetY
            canvas.drawText(text.first)
        }
    }

    /**
     * ● 绘制底部文本
     *
     * ● 2023-11-04 17:54:00 周六 下午
     * @author crowforkotlin
     */
    private inline fun drawBottomText(canvas: Canvas, text: Pair<String, Float>, textListSize: Int, onIniTextX: (Float) -> Unit) {
        if (mMultiLineEnable && textListSize > 1) {
            val heightHalf = height shr 1
            val marginRow = if (mMarginRow >= heightHalf) heightHalf.toFloat() else mMarginRow
            val textHeight = getTextHeight(mTextPaint.fontMetrics)
            val textHeightWithMargin = textHeight + marginRow
            val maxLine =  if (height < textHeightWithMargin) 1 else (measuredHeight / textHeightWithMargin).toInt()
            val listSize = mList.size
            val startPos = (mListPosition + 1) * maxLine
            val endPos = mListPosition * maxLine
            var pos = if (listSize >= startPos) startPos - 1 else listSize - 1
            val textYIncrement = textHeight + marginRow
            repeat(maxLine) {
                if (pos >= endPos) {
                    val currentText = mList[pos]
                    onIniTextX(currentText.second)
                    canvas.drawText(currentText.first)
                    pos --
                    mTextY -= textYIncrement
                } else return
            }
        } else {
            onIniTextX(text.second)
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
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_ATOP)
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