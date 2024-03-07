@file:Suppress(
    "unused",
    "SpellCheckingInspection",
    "MemberVisibilityCanBePrivate",
    "NewApi",
    "SameParameterValue",
    "DEPRECATION"
)

package com.crow.mangax.ui.text

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Region
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.ViewTreeObserver
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import com.crow.base.tools.extensions.log
import com.crow.mangax.R
import com.crow.mangax.ui.text.asHandler
import com.crow.mangax.ui.text.sendMessage
import com.crow.mangax.ui.text.withApiO
import com.crow.mangax.ui.text.withPath
import com.crow.mangax.ui.text.withSizeUnit
import kotlin.math.ceil
import kotlin.properties.Delegates

/**
 * ⦁ 属性文本组件 -- 布局
 * 动画更新说明：视图A和视图B，当动画执行时A和B呼唤身份，此时当前的动画为B，A被设置为下一个视图，如此反复交换实现视图AB的多种动画效果
 * 文本更新说明：设置mText属性触发绘制更新文本、根据文本策略执行对应的效果
 *
 * ⦁ 2023/10/30 15:53
 *
 * @author crowforkotlin
 * @formatter:off
 */
class AttrTextLayout : FrameLayout, IAttrText {

    internal open inner class AttrAnimatorListener(val mAnimatorSet: AnimatorSet) : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator) {
            updateViewPosition()
            updateTextListPosition()
            mAnimationUpdateListener?.onAnimationStart(animation)
        }
        override fun onAnimationEnd(animation: Animator) {
            mAnimationUpdateListener?.onAnimationEnd(animation)
            tryReduceAniamtionTaskCount()
        }
        override fun onAnimationCancel(animation: Animator) {
            if (mTextAnimationStrategy == STRATEGY_ANIMATION_UPDATE_CONTINUA) {
                mCurrentDuration = mAnimatorSet.duration - mAnimatorSet.currentPlayTime
            }
            whenAnimationCancel()
            mAnimationUpdateListener?.onAnimationCancel(animation)
        }
        override fun onAnimationRepeat(animation: Animator) {
            mAnimationUpdateListener?.onAnimationRepeat(animation)
        }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) { initAttr(context, attrs ?: return) }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr) { initAttr( context, attrs ?: return) }
    private fun initAttr(context: Context, attributeSet: AttributeSet) {
        context.obtainStyledAttributes(attributeSet, R.styleable.AttrTextLayout).apply {
            val defaultValue = 0
            mTextFontAbsolutePath = getString(R.styleable.AttrTextLayout_textFontAbsolutePath)
            mTextFontAssetsPath = getString(R.styleable.AttrTextLayout_textFontAssetsPath)
            mTextSize = getDimension(R.styleable.AttrTextLayout_textSize, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12f, context.resources.displayMetrics))
            mTextBoldEnable = getBoolean(R.styleable.AttrTextLayout_textBoldEnable, false)
            mTextFakeBoldEnable = getBoolean(R.styleable.AttrTextLayout_textFakeBoldEnable, false)
            mTextItalicEnable = getBoolean(R.styleable.AttrTextLayout_textItalicEnable, false)
            mTextFakeItalicEnable = getBoolean(R.styleable.AttrTextLayout_textFakeItalicEnable, false)
            mTextAntiAliasEnable = getBoolean(R.styleable.AttrTextLayout_textAntiAliasEnable, false)
            mTextMonoSpaceEnable = getBoolean(R.styleable.AttrTextLayout_textMonoSpaceEnable, false)
            mTextMultipleLineEnable = getBoolean(R.styleable.AttrTextLayout_textMultipleLineEnable, false)
            mSingleTextAnimationEnable = getBoolean(R.styleable.AttrTextLayout_singleTextAnimationEnable, false)
            mTextAnimationSpeed = getInt(R.styleable.AttrTextLayout_textAnimationSpeed, defaultValue).toShort()
            mTextRowMargin = getDimensionPixelOffset(R.styleable.AttrTextLayout_textRowMargin, defaultValue).toFloat()
            mTextCharSpacing = getDimensionPixelOffset(R.styleable.AttrTextLayout_textCharSpacing, defaultValue).toFloat()
            mTextResidenceTime = getInt(R.styleable.AttrTextLayout_textResidenceTime, defaultValue).toLong()
            mTextAnimationLeftEnable = getInt(R.styleable.AttrTextLayout_textAnimationX, defaultValue) == defaultValue
            mTextAnimationTopEnable = getInt(R.styleable.AttrTextLayout_textAnimationY, defaultValue) == defaultValue
            mTextLines = getInt(R.styleable.AttrTextLayout_textLines, 1)
            val color = getColor(R.styleable.AttrTextLayout_layoutBackGroundColor, defaultValue)
            if (color != defaultValue) { mLayoutBackgroundColor = color }
            mTextColor = getColor(R.styleable.AttrTextLayout_textColor, mTextColor)
            mTextUpdateStrategy = when(val value = getInt(R.styleable.AttrTextLayout_textUpdateStrategy, STRATEGY_TEXT_UPDATE_LAZY.toInt())) {
                in STRATEGY_TEXT_UPDATE_ALL..STRATEGY_TEXT_UPDATE_CURRENT -> value.toShort()
                else -> error("AttrTextLayout Get Unknow Gravity Value $value!")
            }
            mTextGravity = when(val value = getInt(R.styleable.AttrTextLayout_textGravity, 1)) {
                in GRAVITY_TOP_START..GRAVITY_BOTTOM_END-> value.toByte()
                else -> error("AttrTextLayout Get Unknow Gravity Value $value!")
            }
            mTextGradientDirection = when(val value = getInt(R.styleable.AttrTextLayout_textGradientDirection, defaultValue)) {
                0 -> null
                in GRADIENT_BEVEL..GRADIENT_VERTICAL-> value.toByte()
                else -> error("AttrTextLayout Get Unknow GradientDirection Value $value!")
            }
            mTextSizeUnitStrategy = when(val value = getInt(R.styleable.AttrTextLayout_textSizeUnitStrategy, STRATEGY_DIMENSION_PX_OR_DEFAULT.toInt())) {
                in STRATEGY_DIMENSION_PX_OR_DEFAULT..STRATEGY_DIMENSION_DP_OR_SP -> value.toShort()
                else -> error("AttrTextLayout Get Unknow SizeUnitStrategy Value $value!")
            }
            mTextAnimationMode = when(val value = getInt(R.styleable.AttrTextLayout_textAnimationMode, ANIMATION_DEFAULT.toInt())) {
                in ANIMATION_DEFAULT..ANIMATION_MOVE_Y_HIGH_BRUSH_DRAW -> value.toShort()
                else -> error("AttrTextLayout Get Unknow AnimationMode Value $value!")
            }
            mTextAnimationStrategy = when(val value = getInt(R.styleable.AttrTextLayout_textAnimationStrategy, STRATEGY_ANIMATION_UPDATE_CONTINUA.toInt())) {
                in STRATEGY_ANIMATION_UPDATE_RESTART..STRATEGY_ANIMATION_UPDATE_CONTINUA -> value.toShort()
                else -> error("AttrTextLayout Get Unknow AnimationStrategy Value $value!")
            }
            onInitTextPaint()
            val text = getString(R.styleable.AttrTextLayout_text)
            recycle()
            mText = text ?: return
        }
    }
    companion object {

        private const val NEWLINE_CHAR_FLAG = '\n'
        private const val NEWLINE_CHAR_FLAG_SLASH = '/'
        private const val NEWLINE_CHAR_FLAG_N = 'n'
        private const val MAX_SCROLL_SPEED: Short = 16
        private const val MIN_ANIMATION_DURATION = 1000L
        private const val ANIMATION_DURATION_FIXED_INCREMEN = 500
        private const val REQUIRED_CACHE_SIZE = 2
        private const val MAX_STRING_LENGTH = 1 shl 10
        private val ANIMATION_TOKEN = Object()

        const val GRAVITY_TOP_START: Byte = 1
        const val GRAVITY_TOP_CENTER: Byte = 2
        const val GRAVITY_TOP_END: Byte = 3
        const val GRAVITY_CENTER_START:Byte = 4
        const val GRAVITY_CENTER: Byte = 5
        const val GRAVITY_CENTER_END: Byte = 6
        const val GRAVITY_BOTTOM_START: Byte = 7
        const val GRAVITY_BOTTOM_CENTER: Byte = 8
        const val GRAVITY_BOTTOM_END: Byte = 9

        const val GRADIENT_BEVEL: Byte = 10
        const val GRADIENT_HORIZONTAL: Byte = 11
        const val GRADIENT_VERTICAL: Byte = 12

        private const val FLAG_TEXT: Byte = 30
        private const val FLAG_CHILD_REFRESH: Byte = 31
        private const val FLAG_LAYOUT_REFRESH: Byte = 32
        private const val FLAG_SCROLL_SPEED: Byte = 33
        private const val FLAG_BACKGROUND_COLOR: Byte = 34
        private const val FLAG_FONT_SIZE: Byte = 35

        const val ANIMATION_DEFAULT: Short = 300
        const val ANIMATION_MOVE_X: Short = 301
        const val ANIMATION_MOVE_Y: Short = 302
        const val ANIMATION_FADE: Short = 303
        const val ANIMATION_FADE_SYNC: Short = 304
        const val ANIMATION_CENTER: Short = 305
        const val ANIMATION_ERASE_X: Short = 306
        const val ANIMATION_CONTINUATION_ERASE_X: Short = 307
        const val ANIMATION_ERASE_Y: Short = 308
        const val ANIMATION_CONTINUATION_ERASE_Y: Short = 309
        const val ANIMATION_CROSS_EXTENSION: Short = 310
        const val ANIMATION_CONTINUATION_CROSS_EXTENSION: Short = 311
        const val ANIMATION_OVAL: Short = 312
        const val ANIMATION_CONTINUATION_OVAL: Short = 313
        const val ANIMATION_RHOMBUS: Short = 314
        const val ANIMATION_CONTINUATION_RHOMBUS: Short = 315
        const val ANIMATION_MOVE_X_DRAW: Short = 316
        const val ANIMATION_MOVE_Y_DRAW: Short = 317
        const val ANIMATION_MOVE_X_HIGH_BRUSH_DRAW: Short = 318
        const val ANIMATION_MOVE_Y_HIGH_BRUSH_DRAW: Short = 319

        /**
         * ⦁ 重新加载更新策略：当重新绘制的时候是否重新执行动画
         *
         * ⦁ 2023-11-06 16:02:52 周一 下午
         * @author crowforkotlin
         */
        const val STRATEGY_ANIMATION_UPDATE_RESTART: Short = 602

        /**
         * ⦁ 默认更新策略：当重新绘制的时候继续执行已停止的动画
         *
         * ⦁ 2023-11-06 16:04:22 周一 下午
         * @author crowforkotlin
         */
        const val STRATEGY_ANIMATION_UPDATE_CONTINUA: Short = 603

        /**
         * ⦁ PX策略 和 DP策略
         *
         * ⦁ 2023-12-26 11:36:26 周二 上午
         * @author crowforkotlin
         */
        const val STRATEGY_DIMENSION_PX_OR_DEFAULT: Short = 604
        const val STRATEGY_DIMENSION_DP_OR_SP: Short = 605

        /**
         * ⦁ 默认更新策略：当文本发生改变触发绘制需求时会直接更新绘制视图
         *
         * ⦁ 2023-10-31 14:09:24 周二 下午
         * @author crowforkotlin
         */
        const val STRATEGY_TEXT_UPDATE_ALL: Short = 900

        /**
         * ⦁ 懒加载更新策略：当文本发生改变时 视图正在执行动画则不会更新，否则更新所有视图
         *
         * ⦁ 2023-10-31 14:09:59 周二 下午
         * @author crowforkotlin
         */
        const val STRATEGY_TEXT_UPDATE_LAZY: Short = 901

        /**
         * ⦁ 文本更新策略：当文本发生改变时，只会更新当前视图的文本（不管动画是否停止执行都会进行更新）
         *
         * ⦁ 2024-01-26 17:19:40 周五 下午
         * @author crowforkotlin
         */
        const val STRATEGY_TEXT_UPDATE_CURRENT: Short = 902

        /**
         * ⦁ TaskScope 单例 暂时预留 考虑到文本数据处理采用单一线程解析，最后交由View进行对于处理
         *
         * ⦁ 2023-12-28 15:24:09 周四 下午
         * @author crowforkotlin
         */
        private var mTaskHandlerThread: HandlerThread? = null
        private lateinit var mTaskHandler: Handler

        /**
         * ⦁ 动画任务个数
         *
         * ⦁ 2024-01-29 16:52:10 周一 下午
         * @author crowforkotlin
         */
        private var mAnimationTaskCount: Int = 0

        /**
         * ⦁ 等待动画个数 0 跳过
         *
         * ⦁ 2024-01-29 16:49:10 周一 下午
         * @author crowforkotlin
         */
        var mAwaitAnimationCount = 0

        /**
         * ⦁ 等待动画时检查时间
         *
         * ⦁ 2024-01-29 16:51:46 周一 下午
         * @author crowforkotlin
         */
        var mAwaitCheckAnimationDuration = 1000L

        private fun tryReduceAniamtionTaskCount() {
            if (mAnimationTaskCount > 0) mAnimationTaskCount --
        }
    }

    /**
     * ⦁ Path -- 用于绘制动画
     *
     * ⦁ 2023-12-21 19:15:44 周四 下午
     * @author crowforkotlin
     */
    private val mPath = Path()

    /**
     * ⦁ 是否更新全部View
     *
     * ⦁ 2023-12-25 19:18:11 周一 下午
     * @author crowforkotlin
     */
    private var mUpdateAll: Boolean = false

    /**
     * ⦁ 动画时间比率
     *
     * ⦁ 2023-12-19 17:43:26 周二 下午
     * @author crowforkotlin
     */
    private var mAnimationTimeFraction = 0f

    /**
     * ⦁ 视图动画回调
     *
     * ⦁ 2023-11-01 09:51:11 周三 上午
     * @author crowforkotlin
     */
    private var mViewAnimationRunnable: Runnable? = null

    /**
     * ⦁ 任务回调列表
     *
     * ⦁ 2024-02-20 18:28:28 周二 下午
     * @author crowforkotlin
     */
    private val mTaskListRunnable: MutableList<Runnable> = mutableListOf()

    /**
     * ⦁ 动画启动时间
     *
     * ⦁ 2023-12-19 17:36:37 周二 下午
     * @author crowforkotlin
     */
    private var mAnimationStartTime = 0L

    /**
     * ⦁ 是否完成布局
     *
     * ⦁ 2023-12-04 10:49:29 周一 上午
     * @author crowforkotlin
     */
    private var mLayoutComplete: Boolean = false

    /**
     * ⦁ 当前任务
     *
     * ⦁ 2023-12-04 11:01:30 周一 上午
     * @author crowforkotlin
     */
    private var mTask: MutableList<Byte>? = null

    /**
     * ⦁ 等待任务回调
     *
     * ⦁ 2024-02-21 16:15:34 周三 下午
     * @author crowforkotlin
     */
    private var mAwaitRunnable: Runnable? = null

    /**
     * ⦁ 文本画笔
     *
     * ⦁ 2023-11-01 09:51:41 周三 上午
     * @author crowforkotlin
     */
    private val mTextPaint : TextPaint = TextPaint()

    /**
     * ⦁ 文本列表 -- 存储屏幕上可显示的字符串集合 实现原理是 动态计算字符串宽度和 视图View做判断
     * First : 文本，Second：测量宽度
     *
     * ⦁ 2023-10-31 14:04:26 周二 下午
     * @author crowforkotlin
     */
    private var mList : MutableList<Pair<String, Float>> = mutableListOf()

    /**
     * ⦁ 动画持续时间
     *
     * ⦁ 2023-10-31 13:59:35 周二 下午
     * @author crowforkotlin
     */
    private var mAnimationDuration: Long = 8000L

    /**
     * ⦁ 当前正在执行的视图动画，没有动画则为空
     *
     * ⦁ 2023-10-31 14:08:33 周二 下午
     * @author crowforkotlin
     */
    private var mViewAnimatorSet : AnimatorSet? = null

    /**
     * ⦁ 上一个动画值
     *
     * ⦁ 2023-11-02 17:16:40 周四 下午
     * @author crowforkotlin
     */
    private var mLastAnimation: Short = -1

    /**
     * ⦁ 当前持续时间
     *
     * ⦁ 2023-11-06 19:14:20 周一 下午
     * @author crowforkotlin
     */
    private var mCurrentDuration = mAnimationDuration

    /**
     * ⦁ 缓存AttrTextView （默认两个）
     *
     * ⦁ 2023-11-01 09:53:01 周三 上午
     * @author crowforkotlin
     */
    private val mCacheViews = ArrayList<AttrTextView>(REQUIRED_CACHE_SIZE)

    /**
     * ⦁ 当前视图的位置
     *
     * ⦁ 2023-11-01 10:12:30 周三 上午
     * @author crowforkotlin
     */
    private var mCurrentViewPos: Int by Delegates.observable(0) { _, _, _ -> onVariableChanged(FLAG_LAYOUT_REFRESH) }

    /**
     * ⦁ 文本列表位置 -- 设置后会触发重新绘制
     *
     * ⦁ 2023-10-31 14:06:16 周二 下午
     * @author crowforkotlin
     */
    private var mListPosition : Int by Delegates.observable(0) { _, _, _ -> onVariableChanged( FLAG_CHILD_REFRESH) }

    /**
     * ⦁ 多行文本（换行）位置
     *
     * ⦁ 2023-11-03 18:19:24 周五 下午
     * @author crowforkotlin
     */
    private var mMultipleLinePos: Int by Delegates.observable(0) { _, _, _ -> onVariableChanged( FLAG_CHILD_REFRESH) }

    /**
     * ⦁ 文本行数 需要先开启多行
     *
     * ⦁ 2024-02-20 13:55:30 周二 下午
     * @author crowforkotlin
     */
    var mTextLines: Int = 1

    /**
     * ⦁ 动画更新监听器
     *
     * ⦁ 2024-01-26 17:33:06 周五 下午
     * @author crowforkotlin
     */
    private var mAnimationUpdateListener: Animator.AnimatorListener? = null
    private var mTypeface: Typeface? = null

    /**
     * ⦁ Async Handler
     *
     * ⦁ 2024-02-20 16:17:18 周二 下午
     * @author crowforkotlin
     */
    private var mHandler: Handler? = null
    private var mHandlerTaskList: MutableList<Runnable> = mutableListOf()

    /**
     * ⦁ 字体类型路径
     *
     * ⦁ 2024-02-01 17:50:05 周四 下午
     * @author crowforkotlin
     */
    var mTextFontAbsolutePath: String? = null
    var mTextFontAssetsPath: String? = null

    /**
     * ⦁ 滚动速度 --- 设置滚动速度实际上是对动画持续时间进行设置 重写SET函数，实现滚动速度设置 对动画时间进行相对的设置，设置后会触发重新绘制 IntRange(from = 1, to = 15)
     *
     * ⦁ 2023-10-31 13:59:53 周二 下午
     * @author crowforkotlin
     */
    var mTextAnimationSpeed: Short by Delegates.observable(1) { _, _, _ -> onVariableChanged(FLAG_SCROLL_SPEED) }

    /**
     * ⦁ 文本内容 -- 设置后会触发重新绘制
     *
     * ⦁ 2023-10-31 14:03:56 周二 下午
     * @author crowforkotlin
     */
    var mText: String by Delegates.observable("") { _, _, _ ->
        if (!mLayoutComplete) {
            addTask(FLAG_TEXT)
        } else {
            onVariableChanged(FLAG_TEXT)
        }
    }

    /**
     * ⦁ Layout的背景颜色
     *
     * ⦁ 2023-11-09 09:47:58 周四 上午
     * @author crowforkotlin
     */
    var mLayoutBackgroundColor: Int by Delegates.observable(Color.BLACK) { _, _, _ -> onVariableChanged(FLAG_BACKGROUND_COLOR) }

    /**
     * ⦁ 是否开启换行
     *
     * ⦁ 2023-10-31 17:31:20 周二 下午
     * @author crowforkotlin
     */
    var mTextMultipleLineEnable: Boolean = false

    /**
     * ⦁ 文字大小 -- 设置后会触发重新绘制 FloatRange(from = 12.0, to = 768.0)
     *
     * ⦁ 2023-10-31 14:03:04 周二 下午
     * @author crowforkotlin
     */
    var mTextSize: Float by Delegates.observable(12f) { _, _, _ ->
        if (!mLayoutComplete) {
            addTask(FLAG_FONT_SIZE)
        } else {
            onVariableChanged(FLAG_FONT_SIZE)
        }
    }

    /**
     * ⦁ 视图对齐方式 -- 上中下 IntRange(from = 1000, to = 1008)
     *
     * ⦁ 2023-10-31 15:24:43 周二 下午
     * @author crowforkotlin
     */
    var mTextGravity: Byte = GRAVITY_TOP_START

    /**
     * ⦁ 字体颜色
     *
     * ⦁ 2023-11-09 09:47:58 周四 上午
     * @author crowforkotlin
     */
    var mTextColor: Int = Color.RED

    /**
     * ⦁ 是否开启抗锯齿
     *
     * ⦁ 2023-11-09 14:42:36 周四 下午
     * @author crowforkotlin
     */
    var mTextAntiAliasEnable: Boolean = false

    /**
     * ⦁ 更新策略 详细可看定义声明
     *
     * ⦁ 2023-10-31 14:07:36 周二 下午
     * @author crowforkotlin
     */
    var mTextUpdateStrategy : Short = STRATEGY_TEXT_UPDATE_ALL

    /**
     * ⦁ 动画策略 详细可查看定义声明
     *
     * ⦁ 2023-11-06 19:29:33 周一 下午
     * @author crowforkotlin
     */
    var mTextAnimationStrategy : Short = STRATEGY_ANIMATION_UPDATE_CONTINUA

    /**
     * ⦁ 是否启用单行动画（当文本 刚好当前页面显示完 根据该值决定是否启用动画）
     *
     * ⦁ 2023-11-02 17:13:40 周四 下午
     * @author crowforkotlin
     */
    var mSingleTextAnimationEnable: Boolean = true

    /**
     * ⦁ 停留时间 静止时生效
     *
     * ⦁ 2023-10-31 13:59:29 周二 下午
     * @author crowforkotlin
     */
    var mTextResidenceTime: Long = 5000

    /**
     * ⦁ 字体假粗体 -- 通过算法渲染实现 性能会比设置样式略低
     *
     * ⦁ 2023-11-10 14:34:58 周五 下午
     * @author crowforkotlin
     */
    var mTextFakeBoldEnable: Boolean = false

    /**
     * ⦁ 字体假斜体 -- 通过变换字体 实现斜体 资源同样比定义好的样式低
     *
     * ⦁ 2023-11-10 14:35:09 周五 下午
     * @author crowforkotlin
     */
    var mTextFakeItalicEnable: Boolean = false

    /**
     * ⦁ 字体粗体样式
     *
     * ⦁ 2023-12-28 18:32:35 周四 下午
     * @author crowforkotlin
     */
    var mTextBoldEnable: Boolean = false

    /**
     * ⦁ 字体斜体样式
     *
     * ⦁ 2023-12-28 18:32:49 周四 下午
     * @author crowforkotlin
     */
    var mTextItalicEnable: Boolean = false

    /**
     * ⦁ 启用等宽字体MonoSpace
     *
     * ⦁ 2023-11-10 14:42:01 周五 下午
     * @author crowforkotlin
     */
    var mTextMonoSpaceEnable: Boolean = false

    /**
     * ⦁ 文本间距
     *
     * ⦁ 2023-12-25 18:05:41 周一 下午
     * @author crowforkotlin
     */
    var mTextCharSpacing: Float = 0f

    /**
     * ⦁ 渐变方向
     *
     * ⦁ 2024-01-02 18:26:09 周二 下午
     * @author crowforkotlin
     */
    var mTextGradientDirection: Byte? = null

    /**
     * ⦁ 动画模式（一般是默认）
     *
     * ⦁ 2023-10-31 18:06:32 周二 下午
     * @author crowforkotlin
     */
    override var mTextAnimationMode: Short = ANIMATION_DEFAULT

    /**
     * ⦁ 动画X方向
     *
     * ⦁ 2023-11-02 14:53:24 周四 下午
     * @author crowforkotlin
     */
    override var mTextAnimationLeftEnable: Boolean = false

    /**
     * ⦁ 动画Y方向
     *
     * ⦁ 2023-11-02 14:53:45 周四 下午
     * @author crowforkotlin
     */
    override var mTextAnimationTopEnable: Boolean = false

    /**
     * ⦁ 每一行的行间距
     *
     * ⦁ 2023-12-25 15:18:34 周一 下午
     * @author crowforkotlin
     */
    override var mTextRowMargin: Float = 0f

    /**
     * ⦁ 当前尺寸大小策略 默认PX
     *
     * ⦁ 2023-12-26 11:37:20 周二 上午
     * @author crowforkotlin
     */
    override var mTextSizeUnitStrategy: Short = STRATEGY_DIMENSION_PX_OR_DEFAULT

    /**
     * ⦁ 初始化画笔
     *
     * ⦁ 2023-11-10 14:35:22 周五 下午
     * @author crowforkotlin
     */
    init {
        /*
        * 这里一定要设置xfermode（在源图像中显示目标图像，目标图像仅在源图像上显示）
        * 否则使用Canvas绘制的动画例如子View实现的 就会导致clipRect的时候文字出现边角出现缺失
        * */
        mHandler = Looper.getMainLooper().asHandler(true)
        mTextPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.OVERLAY)
        mTextPaint.color = mTextColor
        mTextPaint.textSize = mTextSize
        mTextPaint.typeface = if (mTextMonoSpaceEnable) Typeface.MONOSPACE else Typeface.DEFAULT
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                mLayoutComplete = true
                mTask?.let { task ->
                    task.forEach(::onVariableChanged)
                    task.clear()
                    mTask = null
                }
            }
        })
        if (mTaskHandlerThread == null) {
            val thread = HandlerThread("TASK").also { mTaskHandlerThread = it }
           thread.start()
            mTaskHandler = thread.looper.asHandler(true         )
        }
    }

    /**
     * ⦁ 重写 `onMeasure` 方法以自定义视图的测量逻辑。
     *
     * ⦁ 2024-02-21 09:37:34 周三 上午
     * @author crowforkotlin
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        // 检查宽度和高度是否被设置为wrap_content
        val widthIsWrapContent = MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST
        val heightIsWrapContent = MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST
        val width: Int
        val height: Int
        onInitTextPaint()
        if (widthIsWrapContent) {
            val screenWidth = resources.displayMetrics.widthPixels
            width = with(mTextPaint.measureText(mText)) { if (this > screenWidth) screenWidth else ceil(this).toInt() }
        } else {
            width = measuredWidth
        }
        if (heightIsWrapContent) {
            height = with(context.getExactlyTextHeight(mTextPaint.fontMetrics)) { ceil(if (mTextMultipleLineEnable) this * mTextLines else this).toInt() }
        } else {
            height = measuredHeight
        }
        updateTextAndSpec(width, height)
    }

    /**
     * ⦁ 绘制周期
     *
     * ⦁ 2023-12-19 17:39:02 周二 下午
     * @author crowforkotlin
     */
    override fun dispatchDraw(canvas: Canvas) {
        drawAnimation(canvas)
        super.dispatchDraw(canvas)
    }

    /**
     * ⦁ 窗口分离
     *
     * ⦁ 2023-10-31 18:11:26 周二 下午
     * @author crowforkotlin
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        "onDetachedFromWindow".debugLog()
        runCatching {
            val hashCode = this@AttrTextLayout.hashCode()
            mHandler?.apply {
                removeCallbacksAndMessages(this)
                mHandlerTaskList.forEach(::removeCallbacks)
                mHandler = null
            }
            mTaskListRunnable.forEach(mTaskHandler::removeCallbacks)
            mTaskListRunnable.clear()
            mTaskHandler.removeMessages(hashCode)
            mTaskHandler.removeMessages(hashCode + FLAG_TEXT)
            removeAnimationRunnable()
            cancelAnimator()
            removeCallbacks(mViewAnimationRunnable)
            mCacheViews.clear()
            mList.clear()
            mTask?.clear()
            mLastAnimation = -1
        }
    }

    /**
     * ⦁ 更新文本以及测量间距
     *
     * ⦁ 2024-02-21 09:41:50 周三 上午
     * @author crowforkotlin
     */
    private fun updateTextAndSpec(width: Int, height: Int) {
        setMeasuredDimension(width, height)
        if (mCacheViews.size != REQUIRED_CACHE_SIZE) return
        getTextLists(mText) {
            mList = it
            onNotifyLayoutUpdate()
            onNotifyViewUpdate()
            val widhtSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
            val heightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
            mCacheViews[0].measure(widhtSpec, heightSpec)
            mCacheViews[1].measure(widhtSpec, heightSpec)
        }
    }

    /**
     * ⦁ 初始化属性文本视图
     *
     * ⦁ 2023-11-08 11:24:35 周三 上午
     * @author crowforkotlin
     */
    private fun creatAttrTextView(): AttrTextView {
        return AttrTextView(context).also { view ->
            view.mHandler = mHandler!!
            view.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            onInitAttrTextViewValue(view)
            view.mMultiLineEnable = mTextMultipleLineEnable
            view.mGravity = mTextGravity
            addView(view)
        }
    }

    /**
     * ⦁ 值发生变化 执行对于的Logic
     *
     * ⦁ 2023-10-31 14:14:18 周二 下午
     * @author crowforkotlin
     */
    private fun onVariableChanged(flag: Byte) {

        // 根据FLAG 执行对于Logic
        when(flag) {
            FLAG_LAYOUT_REFRESH -> { onNotifyLayoutUpdate() }
            FLAG_CHILD_REFRESH -> { onNotifyViewUpdate() }
            FLAG_TEXT -> {
                getTextLists(if (mText.length > MAX_STRING_LENGTH) { mText.substring(0, MAX_STRING_LENGTH) } else mText) {it ->
                    mHandler?.post(object : Runnable {
                        override fun run() {
                            mList = it
                            var firstInit = false
                            val currentCacheViewSize = mCacheViews.size
                            if (currentCacheViewSize < REQUIRED_CACHE_SIZE) {
                                val viewsToAdd = REQUIRED_CACHE_SIZE - currentCacheViewSize
                                for (index in 0 until  viewsToAdd) { mCacheViews.add(creatAttrTextView()) }
                                firstInit = true
                                com.crow.mangax.ui.text.debug { mCacheViews.forEachIndexed { index, baseAttrTextView -> baseAttrTextView.tag = index } }
                            }
                            if (mList.isEmpty() && firstInit) return
                            onUpdatePosOrView()
                            onNotifyLayoutUpdate()
                            mTaskListRunnable.remove(this)
                        }
                    }.also { runnable -> mTaskListRunnable.add(runnable) })
                }
            }
            FLAG_SCROLL_SPEED -> {
                // 根据 mScrollSpeed 动态调整 mAnimationDuration
                val baseDuration = MAX_SCROLL_SPEED - mTextAnimationSpeed
                mAnimationDuration = if (baseDuration <= 1) MIN_ANIMATION_DURATION else MIN_ANIMATION_DURATION + (ANIMATION_DURATION_FIXED_INCREMEN * baseDuration)
                mCurrentDuration = mAnimationDuration
            }
            FLAG_BACKGROUND_COLOR -> {
                setBackgroundColor(mLayoutBackgroundColor)
            }
            FLAG_FONT_SIZE -> {
                var fontSize: Float
                val paintFontsize = withSizeUnit(pxOrDefault = {
                    fontSize = mTextSize
                    mTextPaint.textSize = mTextSize
                    mTextPaint.textSize
                }, dpOrSp = {
                    fontSize = context.px2sp(mTextSize)
                    mTextPaint.textSize = fontSize
                    context.px2sp(mTextPaint.textSize)
                })
                val textHeight = context.getExactlyTextHeight(mTextPaint.fontMetrics)
                val textWidth = mTextPaint.measureText("O")
                if (textWidth > width || textHeight > height) {
                    "textsize is error $mTextSize \t textWidth is $textWidth \t textHeight is $textHeight \t width is $width \t height is $height".debugLog()
                }
                mTextPaint.textSize = paintFontsize
            }
        }
    }

    /**
     * ⦁ 策略为重刷则 重新通知布局更新
     *
     * ⦁ 2023-12-19 17:37:08 周二 下午
     * @author crowforkotlin
     */
    private fun onUpdateIfResetAnimation() {
        if (mTextAnimationStrategy == STRATEGY_ANIMATION_UPDATE_RESTART) {
            mLastAnimation = -1
            onNotifyLayoutUpdate(false)
        }
    }

    /**
     * ⦁ 更新当前文本位置 或者 视图 （当列表被更新 可能 会小于当前的列表位置 就直接 替换成最后一个， 相对会继续触发ChildView的更新
     *
     * ⦁ 2023-11-01 17:34:08 周三 下午
     * @author crowforkotlin
     */
    private fun onUpdatePosOrView(updateAll: Boolean = false, forceUpdate: Boolean = false) {
        val size = mList.size
        when {
            size <= if (mTextMultipleLineEnable) mMultipleLinePos else mListPosition -> {
                mUpdateAll = mTextUpdateStrategy == STRATEGY_TEXT_UPDATE_ALL
                if (mTextMultipleLineEnable) mMultipleLinePos = 0 else mListPosition = size - 1
            }
            forceUpdate -> onNotifyViewUpdate(updateAll = updateAll)
            mTextUpdateStrategy == STRATEGY_TEXT_UPDATE_CURRENT -> {
                onNotifyViewUpdate(updateAll = false)
            }
            mTextUpdateStrategy == STRATEGY_TEXT_UPDATE_LAZY && mTextAnimationMode in ANIMATION_MOVE_X_HIGH_BRUSH_DRAW..ANIMATION_MOVE_Y_HIGH_BRUSH_DRAW -> {
                onNotifyViewUpdate(updateAll = false)
            }
            mTextUpdateStrategy == STRATEGY_TEXT_UPDATE_LAZY && (mViewAnimatorSet == null || mViewAnimatorSet?.isRunning == false) -> {
                onNotifyViewUpdate(updateAll = true)
            }
            mTextUpdateStrategy == STRATEGY_TEXT_UPDATE_ALL || mTextAnimationMode == ANIMATION_DEFAULT-> {
                onNotifyViewUpdate(updateAll = true)
            }
        }
    }

    /**
     * ⦁ 文本列表是否只占满一个页面
     *
     * ⦁ 2023-11-06 10:53:23 周一 上午
     * @author crowforkotlin
     */
    private fun isListSizeFitPage(): Boolean {
        return if (mTextMultipleLineEnable) {
            val textMaxLine = (measuredHeight / context.getExactlyTextHeight(mTextPaint.fontMetrics)).toInt()
            if (textMaxLine <= 0) return false
            val textListSize = mList.size
            var totalCount: Int = textListSize / textMaxLine
            if (textListSize  % textMaxLine != 0) { totalCount ++ }
            totalCount == 1
        } else {
            mList.size == 1
        }
    }

    /**
     * ⦁ 通知视图View的可见性改变
     *
     * ⦁ 2023-11-01 19:13:58 周三 下午
     * @author crowforkotlin
     */
    private fun onNotifyViewVisibility(pos: Int) {
        val viewA = mCacheViews[pos]
        val viewB = getNextView(pos)
        viewA.visibility = VISIBLE
        viewB.visibility = INVISIBLE
    }

    /**
     * ⦁ 获取上一个 或 下一个 View
     *
     * ⦁ 2023-11-02 10:44:24 周四 上午
     * @author crowforkotlin
     */
    private fun getNextView(pos: Int): AttrTextView {
        return if (pos < mCacheViews.size - 1) {
            mCacheViews[pos + 1]
        } else {
            mCacheViews[pos - 1]
        }
    }

    /**
     * ⦁ 通知视图View更新 如果动画模式 不是 静止切换 代表 当前视图和（上一个视图）需要动态更新 否则 只有当前视图才更新
     *
     * ⦁ 2023-11-01 19:13:46 周三 下午
     * @author crowforkotlin
     */
    private fun onNotifyViewUpdate(updateAll: Boolean = mUpdateAll) {
        if (mList.isEmpty() || mCacheViews.isEmpty() || mCurrentViewPos > mCacheViews.size - 1) return
        val viewA = mCacheViews[mCurrentViewPos]
        val list : MutableList<Pair<String, Float>> = mList.toMutableList()
        viewA.mList = list
        if (mText.isEmpty()) {
            val viewB = getNextView(mCurrentViewPos)
            viewB.mList = list
            if (mTextMultipleLineEnable) {
                viewA.mListPosition = mMultipleLinePos
                viewB.mListPosition = mMultipleLinePos
            } else {
                viewA.mListPosition = mListPosition
                viewB.mListPosition = mListPosition
            }
        } else {
            val pos = if (mTextMultipleLineEnable) mMultipleLinePos else mListPosition
            viewA.mListPosition = pos
            if (updateAll) {
                mUpdateAll = false
                val viewB = getNextView(mCurrentViewPos)
                viewB.mList = mList
                viewB.mListPosition = pos
            }
        }
    }

    /**
     * ⦁ 动态计算可容纳字符个数获取文本列表
     *
     * ⦁ 2023-10-31 13:34:58 周二 下午
     * @author crowforkotlin
     */
    private fun getTextLists(originText: String, onComplete: (MutableList<Pair<String, Float>>) -> Unit) {
        mTaskHandler.sendMessage(object : Runnable {
            override fun run() {
                val viewMeasureWidth = measuredWidth
                var textStringWidth = 0f
                val textStringBuilder = StringBuilder()
                val textList: MutableList<Pair<String, Float>> = mutableListOf()
                val textMaxIndex = originText.length - 1
                onInitTextPaint()
                originText.forEachIndexed { index, char ->
                    val textWidth = mTextPaint.measureText(char.toString(), 0, 1)
                    textStringWidth += textWidth
                    // 字符串宽度 < 测量宽度 假设宽度是 128  那么范围在 0 - 127 故用小于号而不是小于等于
                    if (textStringWidth < viewMeasureWidth) {
                        when (char) {
                            NEWLINE_CHAR_FLAG -> {
                                textList.add(textStringBuilder.toString() to textStringWidth - textWidth)
                                textStringBuilder.clear()
                                textStringWidth = 0f
                            }

                            NEWLINE_CHAR_FLAG_SLASH -> {
                                if (originText.getOrNull(index + 1) == NEWLINE_CHAR_FLAG_N) {
                                    textList.add(textStringBuilder.toString() to textStringWidth - textWidth)
                                    textStringBuilder.clear()
                                    textStringWidth = 0f
                                }
                            }

                            NEWLINE_CHAR_FLAG_N -> {
                                if (index == textMaxIndex) {
                                    textStringWidth =
                                        if (originText.getOrNull(index - 1) != NEWLINE_CHAR_FLAG_SLASH) {
                                            textStringBuilder.append(char)
                                            textList.add(textStringBuilder.toString() to textStringWidth)
                                            0f
                                        } else {
                                            0f
                                        }
                                } else {
                                    if (originText.getOrNull(index - 1) != NEWLINE_CHAR_FLAG_SLASH) {
                                        textStringBuilder.append(char)
                                    } else {
                                        textStringWidth = 0f
                                    }
                                }
                            }

                            else -> {
                                if (index == textMaxIndex) {
                                    textStringBuilder.append(char)
                                    textList.add(textStringBuilder.toString() to textStringWidth)
                                    textStringWidth = 0f
                                } else {
                                    textStringBuilder.append(char)
                                }
                            }
                        }
                    } else {
                        when (char) {
                            NEWLINE_CHAR_FLAG_SLASH -> {
                                if (originText.getOrNull(index + 1) == NEWLINE_CHAR_FLAG_N) {
                                    textList.add(textStringBuilder.toString() to textStringWidth - textWidth)
                                    textStringBuilder.clear()
                                    textStringWidth = 0f
                                }
                            }

                            NEWLINE_CHAR_FLAG_N -> {
                                if (originText.getOrNull(index - 1) != NEWLINE_CHAR_FLAG_SLASH) {
                                    textList.add(textStringBuilder.toString() to textStringWidth - textWidth)
                                    textStringBuilder.clear()
                                    textStringBuilder.append(char)
                                    if (index == textMaxIndex) {
                                        textList.add(textStringBuilder.toString() to textWidth)
                                    } else {
                                        textStringWidth = textWidth
                                    }
                                } else {
                                    textStringWidth = 0f
                                }
                            }

                            else -> {
                                textList.add(textStringBuilder.toString() to textStringWidth - textWidth)
                                textStringBuilder.clear()
                                textStringBuilder.append(char)
                                if (index == textMaxIndex) {
                                    textList.add(textStringBuilder.toString() to textWidth)
                                } else {
                                    textStringWidth = textWidth
                                }
                            }
                        }
                    }
                }
                onComplete(textList)
                mTaskListRunnable.remove(this)
            }
        }.also { mTaskListRunnable.add(it) }) { what = this@AttrTextLayout.hashCode() + FLAG_TEXT }
    }

    /**
     * ⦁ 更新ChildView的位置
     *
     * ⦁ 2023-11-02 17:24:43 周四 下午
     * @author crowforkotlin
     */
    private fun updateViewPosition() {
        if (mCurrentViewPos < mCacheViews.size - 1) {
            mCurrentViewPos ++
        } else {
            mCurrentViewPos = 0
        }
    }

    /**
     * ⦁ 更新文本列表的位置
     *
     * ⦁ 2023-11-02 17:24:58 周四 下午
     * @author crowforkotlin
     */
    private fun updateTextListPosition() {
        if (mList.isEmpty()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mTextPaint.letterSpacing = mTextCharSpacing / mTextPaint.textSize
        }
        when(mTextMultipleLineEnable) {
            true -> {
                val textHeightWithMargin = context.getExactlyTextHeight(mTextPaint.fontMetrics)
                val textMaxLine = if(textHeightWithMargin > height) 1 else  (height / textHeightWithMargin).toInt()
                if (textMaxLine <= 0) return
                val textListSize = mList.size
                var textTotalCount: Int = textListSize / textMaxLine
                if (textListSize  % textMaxLine != 0) { textTotalCount ++ }
                if (mMultipleLinePos < textTotalCount - 1) {
                    mMultipleLinePos ++
                } else {
                    mMultipleLinePos = 0
                }
            }
            false ->{
                if (mListPosition < mList.size - 1) {
                    mListPosition ++
                } else {
                    mListPosition = 0
                }
            }
        }
    }

    /**
     * ⦁ 通知视图更新
     *
     * ⦁ 2023-11-01 10:15:07 周三 上午
     * @author crowforkotlin
     */
    private fun onNotifyLayoutUpdate(isDelay: Boolean = true) {
        if (mCacheViews.size < 2) return
        val animationMode = mTextAnimationMode
        if (mLastAnimation == animationMode) return
        else { mLastAnimation = animationMode }

        cancelAnimator()
        removeAnimationRunnable()
        val viewA = mCacheViews[mCurrentViewPos]
        val viewB = getNextView(mCurrentViewPos)

        // 哪怕if逻辑即使再多也不要直接赋值 避免造成重绘影响性能
        if (viewA.alpha != 1f) viewA.alpha = 1f
        if (viewA.scaleX != 1f) viewA.scaleX = 1f
        if (viewA.scaleY != 1f) viewA.scaleY = 1f
        if (viewA.translationX != 0f) viewA.translationX = 0f
        if (viewA.translationY != 0f) viewA.translationY = 0f
        if (viewB.alpha != 1f) viewB.alpha = 1f
        if (viewB.scaleX != 1f) viewB.scaleX = 1f
        if (viewB.scaleY != 1f) viewB.scaleY = 1f
        if (viewB.translationX != 0f) viewB.translationX = 0f
        if (viewB.translationY != 0f) viewB.translationY = 0f
        viewA.mTextAnimationMode = mTextAnimationMode
        viewB.mTextAnimationMode = mTextAnimationMode
        viewA.mTextLines = mTextLines
        viewB.mTextLines = mTextLines
        onLayoutAnimation(animationMode, isDelay, viewA, viewB)
    }

    /**
     * ⦁ 根据动画模式启动布局动画
     *
     * ⦁ 2024-02-21 09:40:58 周三 上午
     * @author crowforkotlin
     */
    private fun onLayoutAnimation(animationMode: Short, delay: Boolean, viewA: AttrTextView, viewB: AttrTextView) {
        if (isListSizeFitPage() && !mSingleTextAnimationEnable) return run {
            if (viewB.visibility == VISIBLE) viewB.visibility = INVISIBLE
            viewA.translationX = 0f
            viewA.translationY = 0f
            viewB.translationX = 0f
            viewB.translationY = 0f
            cancelAnimator()
            removeAnimationRunnable()
        }
        // 实时同步AB的 四个方向
        viewA.mTextAnimationLeftEnable = mTextAnimationLeftEnable
        viewB.mTextAnimationLeftEnable = mTextAnimationLeftEnable
        viewA.mTextAnimationTopEnable = mTextAnimationTopEnable
        viewB.mTextAnimationTopEnable = mTextAnimationTopEnable
        when(animationMode) {
            ANIMATION_DEFAULT -> launchDefaultAnimation(animationMode, isDelay = delay, viewA, viewB)
            ANIMATION_MOVE_X -> launchMoveXAnimation(animationMode, isDelay = delay)
            ANIMATION_MOVE_Y -> launchMoveYAnimation(animationMode, isDelay = delay)
            ANIMATION_FADE -> launchFadeAnimation(animationMode, isDelay = delay, isSync = false)
            ANIMATION_FADE_SYNC -> launchFadeAnimation(animationMode, isDelay = delay, isSync = true)
            ANIMATION_CENTER -> launchCenterAnimation(animationMode, isDelay = delay)
            ANIMATION_ERASE_Y -> launchDrawAnimation(animationMode, isDelay = delay, viewA, viewB)
            ANIMATION_ERASE_X -> launchDrawAnimation(animationMode, isDelay = delay, viewA, viewB)
            ANIMATION_OVAL -> launchDrawAnimation(animationMode, isDelay = delay, viewA, viewB)
            ANIMATION_CROSS_EXTENSION -> launchDrawAnimation(animationMode, isDelay = delay, viewA, viewB)
            ANIMATION_RHOMBUS -> launchDrawAnimation(animationMode, isDelay = delay, viewA, viewB)
            ANIMATION_CONTINUATION_OVAL -> launchContinuousDrawAnimation(animationMode, isDelay = delay)
            ANIMATION_CONTINUATION_CROSS_EXTENSION -> launchContinuousDrawAnimation(animationMode, isDelay = delay)
            ANIMATION_CONTINUATION_ERASE_Y -> launchContinuousDrawAnimation(animationMode, isDelay = delay)
            ANIMATION_CONTINUATION_ERASE_X -> launchContinuousDrawAnimation(animationMode, isDelay = delay)
            ANIMATION_CONTINUATION_RHOMBUS -> launchContinuousDrawAnimation(animationMode, isDelay = delay)
            ANIMATION_MOVE_X_DRAW -> launchContinuousDrawAnimation(animationMode, isDelay = delay)
            ANIMATION_MOVE_Y_DRAW -> launchContinuousDrawAnimation(animationMode, isDelay = delay)
            ANIMATION_MOVE_X_HIGH_BRUSH_DRAW -> launchHighBrushDrawAnimation(animationMode, delay, isX = true)
            ANIMATION_MOVE_Y_HIGH_BRUSH_DRAW -> launchHighBrushDrawAnimation(animationMode, delay,isX = false)
        }
    }

    /**
     * ⦁ 高刷动画
     *
     * ⦁ 2024-01-29 17:00:36 周一 下午
     * @author crowforkotlin
     */
    private fun launchHighBrushDrawAnimation(animationMode: Short, delay: Boolean, isX: Boolean) {
        tryAwaitAnimationTask {
            mViewAnimationRunnable?.let { mHandler?.removeCallbacks(it) }
            mHandler?.postDelayed(Runnable {
                val viewA = mCacheViews[mCurrentViewPos]
                val viewB = getNextView(mCurrentViewPos)
                viewA.setLayerType(LAYER_TYPE_HARDWARE, null)
                viewB.setLayerType(LAYER_TYPE_HARDWARE, null)
                mAnimationStartTime = System.currentTimeMillis()
                mCurrentDuration = mAnimationDuration
                viewA.mAnimationStartTime = mAnimationStartTime
                viewB.mAnimationStartTime = mAnimationStartTime
                viewA.mIsCurrentView = false
                viewB.mIsCurrentView = true
                updateViewPosition()
                updateTextListPosition()
                val duration: Long = with(MAX_SCROLL_SPEED - mTextAnimationSpeed) { if (this == 8) 0L else if (this == 1) 1L else toLong() shl 1 }
                var count = 0
                viewA.launchHighBrushDrawAnimation(isX, duration)
                viewB.launchHighBrushDrawAnimation(isX, duration)
                viewA.setHighBrushSuccessListener { onHighBrushAnimationEnd(++count, animationMode, delay, viewA, viewB) }
                viewB.setHighBrushSuccessListener { onHighBrushAnimationEnd(++count, animationMode, delay, viewA, viewB) }

            }.also { mViewAnimationRunnable = it },  mAnimationDuration)
        }
    }
    private fun onHighBrushAnimationEnd(count: Int, animationMode: Short, delay: Boolean, viewA: AttrTextView, viewB: AttrTextView) {
        if (count == 2) {
            tryReduceAniamtionTaskCount()
            mViewAnimationRunnable?.let { removeCallbacks(it) }
            mHandler?.post(Runnable {
                viewA.setLayerType(LAYER_TYPE_NONE, null)
                viewB.setLayerType(LAYER_TYPE_NONE, null)
                onLayoutAnimation(animationMode, delay, viewA, viewB)
            }.also { mViewAnimationRunnable = it })
        }
    }

    /**
     * ⦁ 默认的动画
     *
     * ⦁ 2023-11-01 09:51:05 周三 上午
     * @author crowforkotlin
     */
    private fun launchDefaultAnimation(animationMode: Short, isDelay: Boolean, viewA: AttrTextView, viewB: AttrTextView) {
        setLayerType(LAYER_TYPE_HARDWARE, null)
        onNotifyViewVisibility(mCurrentViewPos)
        if (isDelay) {
            mViewAnimationRunnable?.let { mHandler?.removeCallbacks(it) }
            mViewAnimationRunnable = Runnable {
                updateViewPosition()
                updateTextListPosition()
                setLayerType(LAYER_TYPE_NONE, null)
                onLayoutAnimation(animationMode, true, viewA, viewB)
            }
            mHandler?.postDelayed(mViewAnimationRunnable!!, if (mTextResidenceTime < 500) 500 else mTextResidenceTime)
        } else {
            updateViewPosition()
            updateTextListPosition()
            setLayerType(LAYER_TYPE_NONE, null)
            onLayoutAnimation(animationMode, true, viewA, viewB)
        }
    }

    /**
     * ⦁ 中心缩放
     *
     * ⦁ 2023-11-08 17:53:58 周三 下午
     * @author crowforkotlin
     */
    private fun launchCenterAnimation(animationMode: Short, isDelay: Boolean) {
        if (isDelay) {
            mViewAnimationRunnable?.let { mHandler?.removeCallbacks(it) }
            mViewAnimationRunnable = Runnable { launchCenterAnimation(animationMode, false) }
            mHandler?.postDelayed(mViewAnimationRunnable!!, mTextResidenceTime)
        } else {
            mViewAnimatorSet?.cancel()
            mViewAnimatorSet = AnimatorSet()
            val viewA = mCacheViews[mCurrentViewPos]
            val viewB = getNextView(mCurrentViewPos)
            val viewAnimationA = ObjectAnimator.ofPropertyValuesHolder(
                viewA,
                PropertyValuesHolder.ofFloat("scaleX", 1.0f, 0.0f), // X轴方向的缩放
                PropertyValuesHolder.ofFloat("scaleY", 1.0f, 0.0f)  // Y轴方向的缩放
            )
            val viewAnimationB = ObjectAnimator.ofPropertyValuesHolder(
                viewB,
                PropertyValuesHolder.ofFloat("scaleX", 0.0f, 1.0f), // X轴方向的缩放
                PropertyValuesHolder.ofFloat("scaleY", 0.0f, 1.0f)  // Y轴方向的缩放
            )
            mViewAnimatorSet?.let { animatorSet ->
                animatorSet.duration = mCurrentDuration
                animatorSet.interpolator = LinearInterpolator()
                animatorSet.playSequentially(viewAnimationA, viewAnimationB)
                animatorSet.addListener(object : AttrAnimatorListener(animatorSet) {
                    override fun onAnimationStart(animation: Animator) {
                        viewA.setLayerType(LAYER_TYPE_HARDWARE, null)
                        viewB.setLayerType(LAYER_TYPE_HARDWARE, null)
                        if (viewA.visibility == INVISIBLE) viewA.visibility = VISIBLE
                        if (viewB.visibility == INVISIBLE) viewB.visibility = VISIBLE
                        viewB.scaleX = 0f
                        viewB.scaleY = 0f
                        mCurrentDuration = mAnimationDuration
                        super.onAnimationStart(animation)
                    }
                    override fun onAnimationEnd(animation: Animator) {
                        viewA.setLayerType(LAYER_TYPE_NONE, null)
                        viewB.setLayerType(LAYER_TYPE_NONE, null)
                        onLayoutAnimation(animationMode, true, viewA, viewB)
                        super.onAnimationEnd(animation)
                    }
                    override fun onAnimationCancel(animation: Animator) {
                        viewA.setLayerType(LAYER_TYPE_NONE, null)
                        viewB.setLayerType(LAYER_TYPE_NONE, null)
                        super.onAnimationCancel(animation)
                    }
                })
                animatorSet.start()
            }
        }
    }

    /**
     * ⦁ X轴移动动画
     *
     * ⦁ 2024-02-21 09:38:55 周三 上午
     * @author crowforkotlin
     */
    private fun launchMoveXAnimation(animationMode: Short, isDelay: Boolean) {
        if (isDelay) {
            mViewAnimationRunnable?.let { mHandler?.removeCallbacks(it) }
            mViewAnimationRunnable = Runnable { launchMoveXAnimation(animationMode, false) }
            mHandler?.postDelayed(mViewAnimationRunnable!!,  mTextResidenceTime)
        } else {
            mViewAnimatorSet?.cancel()
            mViewAnimatorSet = AnimatorSet()
            val viewA = mCacheViews[mCurrentViewPos]
            val viewB = getNextView(mCurrentViewPos)
            val viewAEnd : Float
            val viewBStart : Float
            val viewX = measuredWidth.toFloat()
            when(mTextAnimationLeftEnable) {
                true -> {
                    viewAEnd = -viewX
                    viewBStart = viewX
                    if (viewB.translationX <= 0f && mTextAnimationStrategy == STRATEGY_ANIMATION_UPDATE_CONTINUA) {
                        viewA.translationX = 0f
                        viewB.translationX = viewBStart
                    } else {
                        viewA.translationX = 0f
                        viewB.translationX = viewBStart
                    }
                }
                false -> {
                    viewAEnd = viewX
                    viewBStart = -viewX
                    if (viewB.translationX >= 0f && mTextAnimationStrategy == STRATEGY_ANIMATION_UPDATE_CONTINUA) {
                        viewA.translationX = 0f
                        viewB.translationX = viewBStart
                    } else {
                        viewA.translationX = 0f
                        viewB.translationX = viewBStart
                    }
                }
            }
            val viewAnimationA = ObjectAnimator.ofFloat(viewA, "translationX", viewA.translationX , viewAEnd)
            val viewAnimationB = ObjectAnimator.ofFloat(viewB, "translationX", viewB.translationX, 0f)
            mViewAnimatorSet?.let { animatorSet ->
                animatorSet.duration = mCurrentDuration
                animatorSet.interpolator = LinearInterpolator()
                animatorSet.playTogether(viewAnimationA, viewAnimationB)
                animatorSet.addListener(object : AttrAnimatorListener(animatorSet) {
                    override fun onAnimationStart(animation: Animator) {
                        viewA.setLayerType(LAYER_TYPE_HARDWARE, null)
                        viewB.setLayerType(LAYER_TYPE_HARDWARE, null)
                        if (viewA.visibility == INVISIBLE) viewA.visibility = VISIBLE
                        if (viewB.visibility == INVISIBLE) viewB.visibility = VISIBLE
                        mCurrentDuration = mAnimationDuration
                        super.onAnimationStart(animation)
                    }
                    override fun onAnimationEnd(animation: Animator) {
                        viewA.setLayerType(LAYER_TYPE_NONE, null)
                        viewB.setLayerType(LAYER_TYPE_NONE, null)
                        onLayoutAnimation(animationMode, true, viewA, viewB)
                        super.onAnimationEnd(animation)
                    }
                    override fun onAnimationCancel(animation: Animator) {
                        viewA.setLayerType(LAYER_TYPE_NONE, null)
                        viewB.setLayerType(LAYER_TYPE_NONE, null)
                        super.onAnimationCancel(animation)
                    }
                })
                animatorSet.start()
            }
        }
    }

    /**
     * ⦁ Y轴移动动画
     *
     * ⦁ 2023-11-01 09:51:11 周三 上午
     * @author crowforkotlin
     */
    private fun launchMoveYAnimation(animationMode: Short, isDelay: Boolean) {
        if(isDelay) {
            mViewAnimationRunnable?.let { mHandler?.removeCallbacks(it) }
            mViewAnimationRunnable = Runnable { launchMoveYAnimation(animationMode, false) }
            mHandler?.postDelayed(mViewAnimationRunnable!!, mTextResidenceTime)
        }
        else {
            mViewAnimatorSet?.cancel()
            mViewAnimatorSet = AnimatorSet()
            val viewA = mCacheViews[mCurrentViewPos]
            val viewB = getNextView(mCurrentViewPos)
            val viewAEnd: Float
            val viewBStart: Float
            val viewY = measuredHeight.toFloat()
            when (mTextAnimationTopEnable) {
                true -> {
                    viewAEnd = -viewY
                    viewBStart = viewY
                    if (viewB.translationY <= 0f || viewB.translationY == viewAEnd) {
                        viewA.translationY = 0f
                        viewB.translationY = viewBStart
                    }
                }
                false -> {
                    viewAEnd = viewY
                    viewBStart = -viewY
                    if (viewB.translationY >= 0f || viewB.translationY == viewAEnd) {
                        viewA.translationY = 0f
                        viewB.translationY = viewBStart
                    }
                }
            }
            val viewAnimationA = ObjectAnimator.ofFloat(viewA, "translationY", viewA.translationY, viewAEnd)
            val viewAnimationB = ObjectAnimator.ofFloat(viewB, "translationY", viewB.translationY, 0f)
            mViewAnimatorSet?.let { animatorSet ->
                animatorSet.duration = mCurrentDuration
                animatorSet.interpolator = LinearInterpolator()
                animatorSet.playTogether(viewAnimationA, viewAnimationB)
                animatorSet.addListener(object : AttrAnimatorListener(animatorSet) {
                    override fun onAnimationStart(animation: Animator) {
                        viewA.setLayerType(LAYER_TYPE_NONE, null)
                        viewB.setLayerType(LAYER_TYPE_NONE, null)
                        if (viewA.visibility == INVISIBLE) viewA.visibility = VISIBLE
                        if (viewB.visibility == INVISIBLE) viewB.visibility = VISIBLE
                        mCurrentDuration = mAnimationDuration
                        super.onAnimationStart(animation)
                    }
                    override fun onAnimationEnd(animation: Animator) {
                        viewA.setLayerType(LAYER_TYPE_NONE, null)
                        viewB.setLayerType(LAYER_TYPE_NONE, null)
                        onLayoutAnimation(animationMode, true, viewA, viewB)
                        super.onAnimationEnd(animation)
                    }
                    override fun onAnimationCancel(animation: Animator) {
                        viewA.setLayerType(LAYER_TYPE_NONE, null)
                        viewB.setLayerType(LAYER_TYPE_NONE, null)
                        super.onAnimationCancel(animation)
                    }
                })
                animatorSet.start()
            }
        }
    }

    /**
     * ⦁ 淡入淡出动画
     *
     * ⦁ 2023-11-02 17:24:05 周四 下午
     * @param isSync 是否同步
     * @author crowforkotlin
     */
    private fun launchFadeAnimation(animationMode: Short, isDelay: Boolean, isSync: Boolean) {
        if (isDelay) {
            mViewAnimationRunnable?.let { mHandler?.removeCallbacks(it) }
            mViewAnimationRunnable = Runnable { launchFadeAnimation(animationMode, false, isSync) }
            mHandler?.postDelayed(mViewAnimationRunnable!!, mTextResidenceTime)
        } else {
            mViewAnimatorSet?.cancel()
            mViewAnimatorSet = AnimatorSet()
            val viewA = mCacheViews[mCurrentViewPos]
            val viewB = getNextView(mCurrentViewPos)
            val viewAnimationA = ObjectAnimator.ofFloat(viewA, "alpha", 1f, 0f)
            val viewAnimationB = ObjectAnimator.ofFloat(viewB, "alpha", 0f, 1f)
            mViewAnimatorSet?.let { animatorSet ->
                animatorSet.duration = mAnimationDuration
                animatorSet.interpolator = LinearInterpolator()
                if (isSync) {
                    animatorSet.playSequentially(viewAnimationA, viewAnimationB)
                } else {
                    animatorSet.playTogether(viewAnimationA, viewAnimationB)
                }
                animatorSet.addListener(object : AttrAnimatorListener(animatorSet) {
                    override fun onAnimationStart(animation: Animator) {
                        viewA.setLayerType(LAYER_TYPE_HARDWARE, null)
                        viewB.setLayerType(LAYER_TYPE_HARDWARE, null)
                        if (viewA.visibility == INVISIBLE) viewA.visibility = VISIBLE
                        if (viewB.visibility == INVISIBLE) viewB.visibility = VISIBLE
                        viewB.alpha = 0f
                        super.onAnimationStart(animation)
                    }
                    override fun onAnimationEnd(animation: Animator) {
                        viewA.setLayerType(LAYER_TYPE_NONE, null)
                        viewB.setLayerType(LAYER_TYPE_NONE, null)
                        onLayoutAnimation(animationMode, true, viewA, viewB)
                        super.onAnimationEnd(animation)
                    }
                    override fun onAnimationCancel(animation: Animator) {
                        viewA.setLayerType(LAYER_TYPE_NONE, null)
                        viewB.setLayerType(LAYER_TYPE_NONE, null)
                        super.onAnimationCancel(animation)
                    }
                })
                animatorSet.start()
            }
        }
    }

    /**
     * ⦁ Layout绘制非连续动画
     *
     * ⦁ 2023-12-19 17:37:40 周二 下午
     * @author crowforkotlin
     */
    private fun launchDrawAnimation(animationMode: Short, isDelay: Boolean, viewA: AttrTextView, viewB: AttrTextView) {
        if (isDelay) {
            mViewAnimationRunnable?.let { mHandler?.removeCallbacks(it) }
            mViewAnimationRunnable = Runnable { launchDrawAnimation(animationMode, false, viewA, viewB) }
            mHandler?.postDelayed(mViewAnimationRunnable!!, mTextResidenceTime)
        } else {
            mViewAnimatorSet?.cancel()
            mViewAnimatorSet = AnimatorSet()
            val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
            valueAnimator.addUpdateListener {
                mAnimationTimeFraction = it.animatedFraction
                invalidate()
            }
            valueAnimator.duration = mCurrentDuration
            mViewAnimatorSet?.let { animatorSet ->
                animatorSet.duration = mCurrentDuration
                animatorSet.interpolator = LinearInterpolator()
                animatorSet.play(valueAnimator)
                animatorSet.addListener(object : AttrAnimatorListener(animatorSet) {
                    override fun onAnimationStart(animation: Animator) {
                        setLayerType(LAYER_TYPE_HARDWARE, null)
                        mAnimationStartTime = System.currentTimeMillis()
                        mCurrentDuration = mAnimationDuration
                        super.onAnimationStart(animation)
                        onNotifyViewVisibility(mCurrentViewPos)
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        setLayerType(LAYER_TYPE_NONE, null)
                        onLayoutAnimation(animationMode, true, viewA, viewB)
                        super.onAnimationEnd(animation)
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        setLayerType(LAYER_TYPE_NONE, null)
                        super.onAnimationCancel(animation)
                    }
                })
                animatorSet.start()
            }
        }
    }

    /**
     * ⦁ 子View绘制连续动画
     *
     * ⦁ 2023-12-19 17:37:40 周二 下午
     * @author crowforkotlin
     */
    private fun launchContinuousDrawAnimation(animationMode: Short, isDelay: Boolean) {
        if (isDelay) {
            mViewAnimationRunnable?.let { mHandler?.removeCallbacks(it) }
            mViewAnimationRunnable = Runnable { launchContinuousDrawAnimation(animationMode, false) }
            mHandler?.postDelayed(mViewAnimationRunnable!!, mTextResidenceTime)
            return
        }
        tryAwaitAnimationTask {
            mViewAnimatorSet?.cancel()
            mViewAnimatorSet = AnimatorSet()
            val viewA = mCacheViews[mCurrentViewPos]
            val viewB = getNextView(mCurrentViewPos)
            val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
            valueAnimator.addUpdateListener {
                mAnimationTimeFraction = it.animatedFraction
                viewA.mAnimationTimeFraction = mAnimationTimeFraction
                viewB.mAnimationTimeFraction = mAnimationTimeFraction
                viewA.invalidate()
                viewB.invalidate()
            }
            valueAnimator.duration = mCurrentDuration
            mViewAnimatorSet?.let { animatorSet ->
                animatorSet.duration = mCurrentDuration
                animatorSet.interpolator = LinearInterpolator()
                animatorSet.play(valueAnimator)
                animatorSet.addListener(object : AttrAnimatorListener(animatorSet) {
                    override fun onAnimationStart(animation: Animator) {
                        viewA.setLayerType(LAYER_TYPE_HARDWARE, null)
                        viewB.setLayerType(LAYER_TYPE_HARDWARE, null)
                        mAnimationStartTime = System.currentTimeMillis()
                        mCurrentDuration = mAnimationDuration
                        viewA.mAnimationStartTime = mAnimationStartTime
                        viewB.mAnimationStartTime = mAnimationStartTime
                        viewA.mIsCurrentView = false
                        viewB.mIsCurrentView = true
                        super.onAnimationStart(animation)
                    }
                    override fun onAnimationEnd(animation: Animator) {
                        viewA.setLayerType(LAYER_TYPE_NONE, null)
                        viewB.setLayerType(LAYER_TYPE_NONE, null)
                        onLayoutAnimation(animationMode, true, viewA, viewB)
                        super.onAnimationEnd(animation)
                    }
                    override fun onAnimationCancel(animation: Animator) {
                        viewA.setLayerType(LAYER_TYPE_NONE, null)
                        viewB.setLayerType(LAYER_TYPE_NONE, null)
                        super.onAnimationCancel(animation)
                    }
                })
                animatorSet.start()
            }
        }
    }

    /**
     * ⦁ 设置动画等待个数时 尝试等待阻塞动画执行
     *
     * ⦁ 2024-02-02 14:57:46 周五 下午
     * @author crowforkotlin
     */
    private fun tryAwaitAnimationTask(onComplete: Runnable) {
        mAwaitAnimationCount.debugLog()
        if (mAwaitAnimationCount > 0) {
            val runnable = object : Runnable {
                override fun run() {
                    mTaskListRunnable.remove(this)
                    if (mAnimationTaskCount >= mAwaitAnimationCount) {
                        mTaskListRunnable.add(this)
                        mTaskHandler.postDelayed(this, 1000L)
                    } else {
                        mAnimationTaskCount++
                        mHandler?.post(object : Runnable {
                            override fun run() {
                                onComplete.run()
                                mHandlerTaskList.remove(this)
                            }
                        }.also { mHandlerTaskList.add(it) })
                    }
                    mTaskListRunnable.debugLog()
                }
            }
            mTaskListRunnable.add(runnable)
            mTaskHandler.post(runnable)
        } else { onComplete.run() }
    }

    /**
     * ⦁ 当动画被终止 进行撤回操作
     *
     * ⦁ 2023-11-08 11:22:09 周三 上午
     * @author crowforkotlin
     */
    private fun whenAnimationCancel() {
        if (mCurrentViewPos == 0) {
            mCurrentViewPos = mCacheViews.size - 1
        } else {
            mCurrentViewPos --
        }
        if (mTextMultipleLineEnable) {
            if (mMultipleLinePos == 0) {
                val textMaxLine = (measuredHeight / context.getExactlyTextHeight(mTextPaint.fontMetrics)).toInt()
                val textListSize = mList.size
                var textTotalCount: Int = textListSize / textMaxLine
                if (textListSize  % textMaxLine != 0) { textTotalCount ++ }
                mMultipleLinePos = textTotalCount - 1
            } else {
                mMultipleLinePos --
            }
        } else {
            if (mListPosition == 0) {
                mListPosition = mList.size - 1
            } else {
                mListPosition --
            }
        }
    }

    /**
     * ⦁ 取消动画任务
     *
     * ⦁ 2023-11-02 17:24:00 周四 下午
     * @author crowforkotlin
     */
    private fun removeAnimationRunnable() {
        removeCallbacks(mViewAnimationRunnable)
    }

    /**
     * ⦁ 取消动画
     *
     * ⦁ 2023-11-01 09:51:21 周三 上午
     * @author crowforkotlin
     */
    private fun cancelAnimator() {
        mViewAnimatorSet?.cancel()
        mViewAnimatorSet = null
    }

    /**
     * ⦁ 添加任务
     *
     * ⦁ 2023-12-04 11:02:32 周一 上午
     * @author crowforkotlin
     */
    private fun addTask(flag: Byte) {
        if (mTask == null) mTask = mutableListOf(flag) else mTask?.add(flag)
    }

    /**
     * ⦁ 初始化BaseAttrTextView的基本属性
     *
     * ⦁ 2023-12-22 15:09:52 周五 下午
     * @author crowforkotlin
     */
    private fun onInitAttrTextViewValue(view: AttrTextView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mTextPaint.letterSpacing = mTextCharSpacing / mTextPaint.textSize
        }
        view.mTextSizeUnitStrategy = mTextSizeUnitStrategy
        view.mTextAnimationTopEnable = mTextAnimationTopEnable
        view.mTextAnimationLeftEnable = mTextAnimationLeftEnable
        view.mTextAnimationMode = mTextAnimationMode
        if (mTextAnimationMode !in ANIMATION_ERASE_X..ANIMATION_MOVE_Y_HIGH_BRUSH_DRAW) {
            view.mAnimationStartTime = 0
        }
        view.mTextRowMargin = withSizeUnit(this::mTextRowMargin, dpOrSp = { context.px2dp(mTextRowMargin) })
        view.mTextPaint = mTextPaint
    }

    /**
     * ⦁ 初始化文本画笔
     *
     * ⦁ 2023-12-28 18:33:08 周四 下午
     * @author crowforkotlin
     */
    private fun onInitTextPaint() {
        mTextPaint.apply {
            // 设置线性渐变效果
            val widthFloat = width.toFloat()
            val heightFloat = height.toFloat()
            val halfWidth = width / 2f
            val halfHeight = height / 2f
            shader = when(mTextGradientDirection) {
                GRADIENT_BEVEL -> LinearGradient(0f, 0f, widthFloat, heightFloat, intArrayOf(Color.RED, Color.GREEN, Color.BLUE), null, Shader.TileMode.CLAMP)
                GRADIENT_VERTICAL -> LinearGradient(halfWidth, 0f, halfWidth, heightFloat, intArrayOf(Color.RED, Color.GREEN, Color.BLUE), null, Shader.TileMode.CLAMP)
                GRADIENT_HORIZONTAL -> LinearGradient(0f, halfHeight, widthFloat, halfHeight, intArrayOf(Color.RED, Color.GREEN, Color.BLUE), null, Shader.TileMode.CLAMP)
                else -> { null }
            }
            color = mTextColor
            isAntiAlias = mTextAntiAliasEnable
            textSize = withSizeUnit(this@AttrTextLayout::mTextSize, dpOrSp = { context.px2sp(mTextSize) } )
            isFakeBoldText = mTextFakeBoldEnable
            textSkewX = if (mTextFakeItalicEnable) -0.25f else 0f
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                letterSpacing = mTextCharSpacing / mTextPaint.textSize
            }
            initPaintTypeFace(this)
        }
    }

    /**
     * ⦁ 初始化字体类型样式
     *
     * ⦁ 2024-02-02 11:35:59 周五 上午
     * @author crowforkotlin
     */
    private fun initPaintTypeFace(textPaint: TextPaint) {
        val value = when {
            mTextBoldEnable && mTextItalicEnable -> Typeface.BOLD_ITALIC
            mTextBoldEnable -> Typeface.BOLD
            mTextItalicEnable -> Typeface.ITALIC
            else -> {
                textPaint.typeface = if (mTextMonoSpaceEnable) Typeface.MONOSPACE else Typeface.DEFAULT
                null
            }
        }

        val typeface = runCatching {
            when {
                mTextFontAssetsPath != null -> createTypefaceFromAssets(value)
                mTextFontAbsolutePath != null -> createTypefaceFromFile(value)
                else -> if (value == null) mTypeface else Typeface.create(mTypeface, value)
            }
        }.getOrElse { cause ->
            cause.stackTraceToString().debugLog(level = Log.ERROR)
            Typeface.create(if (mTextMonoSpaceEnable) Typeface.MONOSPACE else Typeface.DEFAULT, value ?: return)
        }

        mTypeface = typeface
        mTextPaint.typeface = typeface
    }
    private fun createTypefaceFromAssets(value: Int?): Typeface {
        val baseTypeface = Typeface.createFromAsset(context.assets, mTextFontAssetsPath)
        return if (value == null) baseTypeface else Typeface.create(baseTypeface, value)
    }
    private fun createTypefaceFromFile(value: Int?): Typeface {
        val baseTypeface = Typeface.createFromFile(mTextFontAbsolutePath)
        return if (value == null) baseTypeface else Typeface.create(baseTypeface, value)
    }

    /**
     * ⦁ 绘制动画
     *
     * ⦁ 2023-12-22 16:01:13 周五 下午
     * @author crowforkotlin
     */
    private fun drawAnimation(canvas: Canvas) {
        if (mAnimationStartTime > 0) {
            when(mTextAnimationMode) {
                ANIMATION_ERASE_X -> { canvas.drawEraseX(width.toFloat(), height.toFloat(), width * mAnimationTimeFraction) }
                ANIMATION_ERASE_Y -> { canvas.drawEraseY(width.toFloat(), height.toFloat(), height * mAnimationTimeFraction) }
                ANIMATION_CROSS_EXTENSION -> { canvas.drawCrossExtension(width, height, mAnimationTimeFraction) }
                ANIMATION_OVAL -> {
                    withPath(mPath) {
                        canvas.drawOval(this, width, height, mAnimationTimeFraction)
                        canvas.clipPath(this)
                    }
                }
                ANIMATION_RHOMBUS -> {
                    withPath(mPath) {
                        canvas.drawRhombus(this, width, height, mAnimationTimeFraction)
                        withApiO(
                            leastO = { canvas.clipOutPath(this) },
                            lessO = { canvas.clipPath(this, Region.Op.XOR) }
                        )
                    }
                }
            }
        }
    }

    /**
     * ⦁ 应用配置 -- 触发View的更新
     *
     * ⦁ 2023-11-02 17:25:43 周四 下午
     * @author crowforkotlin
     */
    fun applyOption() {
        if (mCacheViews.isNotEmpty()) {
            mCacheViews.forEach {  view ->
                onInitAttrTextViewValue(view)
                view.mGravity = mTextGravity
                view.mMultiLineEnable = mTextMultipleLineEnable
            }
            getTextLists(mText) {
                mList = it
                onUpdatePosOrView(updateAll = true)
                onUpdateIfResetAnimation()
            }
        }
    }
    fun setOnAnimationUpdateListener(listener: Animator.AnimatorListener) { mAnimationUpdateListener = listener }
    fun removeOnAnimationUpdateListener() { mAnimationUpdateListener = null }
}