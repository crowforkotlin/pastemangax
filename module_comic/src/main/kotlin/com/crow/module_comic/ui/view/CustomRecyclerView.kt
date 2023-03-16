package com.crow.module_comic.ui.view

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.extensions.logMsg
import kotlin.math.pow
import kotlin.math.sqrt


/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_comic/src/main/kotlin/com/crow/module_comic/ui/fragment
 * @Time: 2023/3/16 13:09
 * @Author: CrowForKotlin
 * @Description:
 * @formatter:on
 **************************/
class CustomRecyclerView : RecyclerView {
    companion object {
        const val TAG = "CustomView"
        const val MAX_SCALE_X = 2f // 最大横向缩放比例
        const val MAX_SCALE_Y = 2f // 最大纵向缩放比例
        const val MIN_SCALE_X = 0.8f // 最小横向缩放比例
        const val MIN_SCALE_Y = 0.8f // 最小纵向缩放比例
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(context, attributeSet, defStyleAttr)

    var mDownX: Float = 0f
    var mDownY: Float = 0f

    private var mid: PointF = PointF(0f, 0f)
    private var startMidDistance: Float = 0f
    private var originScaleX: Float = 1f
    private var originScaleY: Float = 1f

    private val mGestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            "onDobuleTab".logMsg(tag = TAG)
            scaleAnimation(1f, 2f).start()
            requestLayout()
            return true
        }

        override fun onDown(e: MotionEvent): Boolean {
            "onDown".logMsg(tag = TAG)
            return true
        }

        override fun onShowPress(e: MotionEvent) {
            "onShowPress".logMsg(tag = TAG)
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            "onSingleTapUp".logMsg(tag = TAG)
            return true
        }

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            "onScroll".logMsg(tag = TAG)
            // 单指滑动时，更新RecyclerView的位置
            if (e2.pointerCount == 1) {
                val dx = (-distanceX).toInt()
                val dy = (-distanceY).toInt()
                val xOffset = Math.round(dx / this@CustomRecyclerView.getScaleX()).toInt()
                val yOffset = Math.round(dy / this@CustomRecyclerView.getScaleY()).toInt()
                this@CustomRecyclerView.scrollBy(xOffset, yOffset)
            }
            // 双指缩放时，更新RecyclerView的大小
            if (e2.pointerCount == 2) {
                mid.x = (e2.getX(0) + e2.getX(1)) / 2
                mid.y = (e2.getY(0) + e2.getY(1)) / 2
                val distance = sqrt(((e2.getX(0) - e2.getX(1)).toDouble().pow(2.0) +
                        (e2.getY(0) - e2.getY(1)).toDouble().pow(2.0))).toFloat()
                if (startMidDistance == 0f) {
                    startMidDistance = distance
                    originScaleX = this@CustomRecyclerView.scaleX
                    originScaleY = this@CustomRecyclerView.scaleY
                } else {
                    val scale = distance / startMidDistance * if (originScaleX < originScaleY) originScaleX else originScaleY
                    val scaleX = Math.max(MIN_SCALE_X, Math.min(scale, MAX_SCALE_X))
                    val scaleY = Math.max(MIN_SCALE_Y, Math.min(scale, MAX_SCALE_Y))
                    this@CustomRecyclerView.scaleX = scaleX
                    this@CustomRecyclerView.scaleY = scaleY
                }
            }
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            "onLongPress".logMsg(tag = TAG)
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            "onFling".logMsg(tag = TAG)
            return true
        }

        // 缩放动画
        fun scaleAnimation(start: Float, end: Float): AnimatorSet {
            val animator_X = ObjectAnimator.ofFloat(this@CustomRecyclerView, View.SCALE_X, start, end)
            val animator_Y = ObjectAnimator.ofFloat(this@CustomRecyclerView, View.SCALE_Y, start, end)
            val animatorSet = AnimatorSet()
            animatorSet.playTogether(animator_X, animator_Y)
            return animatorSet.apply { duration = 500 }
        }
    }

    private val mScaleGesture by lazy { GestureDetector(context, mGestureListener) }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent): Boolean {
        mScaleGesture.onTouchEvent(e)
        return super.onTouchEvent(e)
    }
}