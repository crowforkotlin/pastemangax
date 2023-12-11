package com.crow.base.ui.view.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import com.crow.base.R
import com.crow.base.tools.extensions.containsFlag
import com.crow.base.tools.extensions.createPaint
import com.crow.base.tools.extensions.doOnCanvas
import com.crow.base.tools.extensions.dp2px
import com.crow.base.tools.extensions.utilReset
import kotlin.math.absoluteValue
import kotlin.properties.Delegates

/**
 * @author: drawf
 * @date: 2019/3/21
 * @see: <a href=""></a>
 * @description: 可设置阴影的布局
 *
 * NOTE: ShadowLayout实际宽度=内容区域宽度+（mShadowRadius + Math.abs(mDx)）*2
 * ShadowLayout实际高度=内容区域高度+（mShadowRadius + Math.abs(mDy)）*2
 * 当只设置一边显示阴影时，阴影部分占用的大小是（mShadowRadius + Math.abs(mDx、mDy)）
 */
class BaseShadowLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {

    private fun interface IBaseShadowChildEvent { fun doOnDrawChild(canvas: Canvas) }
    companion object {
        private const val DEFAULT_BORDER_ENABLE = true
        private const val DEFAULT_SHADOW_COLOR = Color.BLACK
        private const val DEFAULT_SHADOW_RADIUS = 0f
        private const val DEFAULT_SHADOW_SIDES = 0
        private const val DEFAULT_BORDER_COLOR = Color.BLACK
        private const val DEFAULT_BORDER_WIDTH = 0f
        private const val DEFAULT_CORNER_RADIUS = 0f
        private const val DEFAULT_SHADOW_DX = 0f
        private const val DEFAULT_SHADOW_DY = 0f
        private const val FLAG_SIDES_TOP = 1
        private const val FLAG_SIDES_RIGHT = 2
        private const val FLAG_SIDES_BOTTOM = 4
        private const val FLAG_SIDES_LEFT = 8
        private const val FLAG_SIDES_ALL = 15
    }

    //阴影颜色
    @ColorInt
    private var mShadowColor: Int = 0

    // 阴影发散距离 blur
    private var mShadowRadius: Float = 0f

    // x轴偏移距离
    private var mDx: Float = 0f

    // y轴偏移距离
    private var mDy: Float = 0f

    // 圆角半径
    private var mCornerRadius: Float = 0f

    // 边框颜色
    @ColorInt
    private var mBorderColor: Int = 0

    // 边框宽度
    private var mBorderWidth: Float = 0f

    // 开启边框
    private var mBorderEnable: Boolean = false

    // 控制四边是否显示阴影
    private var mShadowSides: Int = 0

    // 全局画笔
    private var mPaint: Paint = createPaint(color = Color.WHITE)

    // 边框画笔
    private val mBorderPaint by lazy {
        Paint().apply {
            isAntiAlias = true                      // 开启抗锯齿效果
            color = mBorderColor               // 设置画笔颜色
            strokeWidth = mBorderWidth   // 设置画笔线宽
            style = Paint.Style.STROKE        // 设置画笔样式 线条
        }
    }

    // 全局Path
    private var mPath = Path()

    // 合成模式
    private val mXfermode: PorterDuffXfermode by lazy { PorterDuffXfermode(PorterDuff.Mode.DST_OUT) }

    // 视图内容区域的RectF实例
    private var mContentRecF: RectF by Delegates.notNull()

    // 视图边框的RectF实例
    private var mBorderRecF: RectF? = null

    init {
        initAttributes(context, attrs)                                                // 初始化自定义属性
        processPadding()                                                               // 处理View的Padding为阴影留出空间
        setLayerType(View.LAYER_TYPE_HARDWARE, null)   //设置硬件渲染类型
    }

    // 初始化自定义属性
    private fun initAttributes(context: Context, attrs: AttributeSet?) {
        val attr = context.obtainStyledAttributes(attrs, R.styleable.BaseShadowLayout)
        try {
            mDx = attr.getDimension(R.styleable.BaseShadowLayout_base_dx, DEFAULT_SHADOW_DX)
            mDy = attr.getDimension(R.styleable.BaseShadowLayout_base_dy, DEFAULT_SHADOW_DY)
            mShadowColor = attr.getColor(R.styleable.BaseShadowLayout_base_shadowColor, DEFAULT_SHADOW_COLOR)
            mShadowRadius = attr.getDimension(R.styleable.BaseShadowLayout_base_shadowRadius, context.dp2px(DEFAULT_SHADOW_RADIUS))
            mCornerRadius = attr.getDimension(R.styleable.BaseShadowLayout_base_cornerRadius, context.dp2px(DEFAULT_CORNER_RADIUS))
            mBorderColor = attr.getColor(R.styleable.BaseShadowLayout_base_borderColor, DEFAULT_BORDER_COLOR)
            mBorderWidth = attr.getDimension(R.styleable.BaseShadowLayout_base_borderWidth, context.dp2px(DEFAULT_BORDER_WIDTH))
            mBorderEnable = attr.getBoolean(R.styleable.BaseShadowLayout_base_borderEnable, DEFAULT_BORDER_ENABLE)
            mShadowSides = attr.getInt(R.styleable.BaseShadowLayout_base_shadowSides, DEFAULT_SHADOW_SIDES)
        } finally {
            attr.recycle()
        }
    }

    // 处理View的Padding为阴影留出空间
    private fun processPadding() {
        val xPadding = (mShadowRadius + mDx.absoluteValue).toInt()
        val yPadding = (mShadowRadius + mDy.absoluteValue).toInt()
        setPadding(
            if (mShadowSides.containsFlag(FLAG_SIDES_LEFT)) xPadding else 0,
            if (mShadowSides.containsFlag(FLAG_SIDES_TOP)) yPadding else 0,
            if (mShadowSides.containsFlag(FLAG_SIDES_RIGHT)) xPadding else 0,
            if (mShadowSides.containsFlag(FLAG_SIDES_BOTTOM)) yPadding else 0
        )
    }

    // 绘制阴影
    private fun drawShadow(canvas: Canvas) {
        canvas.doOnCanvas {
            mPaint.setShadowLayer(mShadowRadius, mDx, mDy, mShadowColor)                    // 设置阴影效果
            canvas.drawRoundRect(mContentRecF, mCornerRadius, mCornerRadius, mPaint)   // 在画布上绘制带阴影的圆角矩形
        }

    }

    // 绘制子View
    private fun drawChild(canvas: Canvas, iBaseShadowChildEvent: IBaseShadowChildEvent) {
        canvas.saveLayer(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), mPaint)   // 保存画布当前状态到图层,并设定图层大小为全屏
        iBaseShadowChildEvent.doOnDrawChild(canvas)
        mPath.addRect(mContentRecF, Path.Direction.CW)                                                              // 构建一个Path,包含一个矩形和内切的圆角矩形,用于混合模式
        mPath.addRoundRect(mContentRecF, mCornerRadius, mCornerRadius, Path.Direction.CW)
        mPath.fillType = Path.FillType.EVEN_ODD
        mPaint.xfermode = mXfermode                                                                                             // 设置混合模式,并绘制Path,实现圆角效果
        canvas.drawPath(mPath, mPaint)
        mPaint.utilReset()                                                                                                                  // 清除画笔
        mPath.reset()                                                                                                                        // 清除路径
        canvas.restore()                                                                                                                    // 恢复画布状态 LAYER被弹出栈
    }

    // 绘制边框
    private fun drawBorder(canvas: Canvas) { if (mBorderRecF != null) { canvas.doOnCanvas { canvas.drawRoundRect(mBorderRecF!!, mCornerRadius, mCornerRadius, mBorderPaint) } } }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mContentRecF = RectF(paddingLeft.toFloat(), paddingTop.toFloat(), (w - paddingRight).toFloat(), (h - paddingBottom).toFloat())

        //以边框宽度的三分之一，微调边框绘制位置，以在边框较宽时得到更好的视觉效果
        val bw = mBorderWidth / 3
        if (bw > 0) {
            mBorderRecF = RectF(
                mContentRecF.left + bw,
                mContentRecF.top + bw,
                mContentRecF.right - bw,
                mContentRecF.bottom - bw
            )
        }
    }

    override fun dispatchDraw(canvas: Canvas) {

        //绘制阴影
        drawShadow(canvas)

        //绘制子View
        drawChild(canvas) { super.dispatchDraw(it) }

        //绘制边框
        if (mBorderEnable) drawBorder(canvas)
    }
}
