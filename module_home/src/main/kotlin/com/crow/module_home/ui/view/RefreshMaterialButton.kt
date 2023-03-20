package com.crow.module_home.ui.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import com.crow.base.tools.extensions.clickGap
import com.crow.module_home.ui.view.RefreshMaterialButton.AnimatorCallback
import com.google.android.material.button.MaterialButton

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_home/src/main/kotlin/com/crow/module_home/ui/view
 * @Time: 2023/3/12 14:12
 * @Author: CrowForKotlin
 * @Description: RefreshMaterialButton 刷新按钮 可用于登录，后期需改进 目前暂不使用
 * @formatter:off
 **************************/
class RefreshMaterialButton : MaterialButton {
    fun interface AnimatorCallback {
        fun onAnimatorStart()
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleArrt: Int) : super(context, attrs, defStyleArrt)

    var mAnimateListener: AnimatorCallback? = null

    private var mArcValueAnimator: ValueAnimator = ValueAnimator.ofInt(0, 1080).apply {
        duration = 3000
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener { valueAnimator ->
            mStartAngle = valueAnimator.animatedValue as Int
            invalidate()
        }
    }
    private var mPaint: Paint = Paint()
    private var mStartAngle = 0
    private var mLeft: Int = 0
    private var mRight: Int = 0
    private lateinit var mAnimateSet: AnimatorSet
    private lateinit var mRect: RectF

    init {
        doOnLayout {
            mLeft = measuredWidth - measuredHeight
            mRight = measuredWidth
            val multiple = measuredHeight / 4
            mRect = RectF(
                mLeft + multiple.toFloat(),
                0f + multiple,
                mRight - multiple.toFloat(),
                (measuredHeight - multiple).toFloat()
            )
        }
        gravity = Gravity.END or Gravity.CENTER
        setTextColor(Color.BLACK)
        background = null
        clickGap { _, _ ->
            isClickable = false
            animateLoading()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mPaint.color = ContextCompat.getColor(context, android.R.color.holo_purple)
        mPaint.strokeWidth = (measuredHeight / 8).toFloat()
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.style = Paint.Style.STROKE
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (mArcValueAnimator.isRunning) {
            canvas?.drawArc(mRect, mStartAngle.toFloat(), 270f, false, mPaint)
        } else {
            canvas?.drawCircle(0f, 0f, 0f, mPaint)
        }
    }

    private fun MaterialButton.animateLoading() {

        // 淡出
        val fadeOutAnimator = ObjectAnimator.ofFloat(this, View.ALPHA, 1f, 0f)
        fadeOutAnimator.duration = 150
        fadeOutAnimator.doOnEnd { text = null }

        // 淡入
        val fadeInAnimator = ObjectAnimator.ofFloat(this, View.ALPHA, 0f, 1f)
        fadeInAnimator.duration = 150
        fadeOutAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) { mAnimateListener?.onAnimatorStart() }
            override fun onAnimationEnd(animation: Animator){}
            override fun onAnimationCancel(animation: Animator){}
            override fun onAnimationRepeat(animation: Animator){}
        })

        // 动画结束后显示进度条
        mAnimateSet = AnimatorSet()
        mAnimateSet.play(mArcValueAnimator).with(fadeInAnimator).after(fadeOutAnimator)
        mAnimateSet.start()
    }

    fun stopAnimateLoading() {
        mAnimateSet.cancel()
        isClickable = true
    }

    inline fun addAnimateStartListener(crossinline start: (MaterialButton) -> Unit) {
        mAnimateListener = AnimatorCallback { start(this) }
    }
}