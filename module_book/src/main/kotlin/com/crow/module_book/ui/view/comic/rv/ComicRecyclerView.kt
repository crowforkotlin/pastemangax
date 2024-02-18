package com.crow.module_book.ui.view.comic.rv

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.doOnEnd
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.tools.extensions.BASE_ANIM_300L
import com.crow.base.tools.extensions.findCenterViewPosition
import kotlin.math.abs

class ComicRecyclerView : RecyclerView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    private inner class ComicRvGestureListener : ComicGestureDetectorWithLongTap.ComicGestureListener() {

        override fun onSingleTapConfirmed(ev: MotionEvent): Boolean {
            mTapListener?.invoke(ev)
            return false
        }

        override fun onDoubleTap(ev: MotionEvent): Boolean {
            mDetector.isDoubleTapping = true
            return false
        }

        override fun onLongTapConfirmed(ev: MotionEvent) {
            if (mLongTapListener?.invoke(ev) == true) {
                performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        }

        fun onDoubleTapConfirmed(ev: MotionEvent) {
            if (!mIsZooming && mDoubleTapZoom) {
                if (scaleX != MIN_SCALE_FACTOR) {
                    zoom(mCurrentScale, MIN_SCALE_FACTOR, x, 0f, y, 0f)
                } else {
                    val toScale = 2f
                    val toX = (mHalfWidth - ev.x) * (toScale - 1)
                    val toY = (mHalfHeight - ev.y) * (toScale - 1)
                    zoom(MIN_SCALE_FACTOR, toScale, 0f, toX, 0f, toY)
                }
            }
        }
    }

    private inner class ComicRvDetector : ComicGestureDetectorWithLongTap(context, mListener) {

        private var scrollPointerId = 0
        private var downX = 0
        private var downY = 0
        private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
        private var isZoomDragging = false
        var isDoubleTapping = false
        var isQuickScaling = false

        override fun onTouchEvent(ev: MotionEvent): Boolean {
            val action = ev.actionMasked
            val actionIndex = ev.actionIndex

            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    scrollPointerId = ev.getPointerId(0)
                    downX = (ev.x + 0.5f).toInt()
                    downY = (ev.y + 0.5f).toInt()
                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                    scrollPointerId = ev.getPointerId(actionIndex)
                    downX = (ev.getX(actionIndex) + 0.5f).toInt()
                    downY = (ev.getY(actionIndex) + 0.5f).toInt()
                }
                MotionEvent.ACTION_MOVE -> {
                    if (isDoubleTapping && isQuickScaling) return true

                    val index = ev.findPointerIndex(scrollPointerId)
                    if (index < 0) {
                        return false
                    }

                    val x = (ev.getX(index) + 0.5f).toInt()
                    val y = (ev.getY(index) + 0.5f).toInt()
                    var dx = x - downX
                    var dy = if (mAtFirstPosition || mAtLastPosition) y - downY else 0

                    if (!isZoomDragging && mCurrentScale > 1f) {
                        var startScroll = false

                        if (abs(dx) > touchSlop) {
                            if (dx < 0) dx += touchSlop else dx -= touchSlop
                            startScroll = true
                        }

                        if (abs(dy) > touchSlop) {
                            if (dy < 0) dy += touchSlop else dy -= touchSlop
                            startScroll = true
                        }

                        if (startScroll) isZoomDragging = true
                    }

                    if (isZoomDragging) {
                        zoomScrollBy(dx, dy)
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (isDoubleTapping && !isQuickScaling) {
                        mListener.onDoubleTapConfirmed(ev)
                    }
                    isZoomDragging = false
                    isDoubleTapping = false
                    isQuickScaling = false
                }
                MotionEvent.ACTION_CANCEL -> {
                    isZoomDragging = false
                    isDoubleTapping = false
                    isQuickScaling = false
                }
            }
            return super.onTouchEvent(ev)
        }
    }

    fun interface IComicPreScroll {

        fun onPreScrollListener(dx: Int, dy: Int, position: Int)
    }

    companion object {
        private const val ANIMATOR_DURATION_TIME = 200
        private const val MIN_SCALE_FACTOR = 0.5f
        private const val MAX_SCALE_FACTOR = 3f
    }

    private var mIsZooming = false
    private var mAtLastPosition = false
    private var mAtFirstPosition = false
    private var mHalfWidth = 0
    private var mHalfHeight = 0
    private var mOriginalHeight: Int = -1
    private var mFirstVisibleItemPosition = 0
    private var mLastVisibleItemPosition = 0
    private var mLastCenterViewPosition = 0
    private var mCurrentScale = MIN_SCALE_FACTOR

    private var mPreScrollListener: IComicPreScroll? = null
    private val mListener = ComicRvGestureListener()
    private val mDetector = ComicRvDetector()

    var mDoubleTapZoom = true
    var mTapListener: ((MotionEvent) -> Unit)? = null
    var mLongTapListener: ((MotionEvent) -> Boolean)? = null

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        mHalfWidth = MeasureSpec.getSize(widthSpec) / 2
        mHalfHeight = (if (mOriginalHeight == -1) {
            mOriginalHeight = MeasureSpec.getSize(heightSpec)
            mOriginalHeight
        } else MeasureSpec.getSize(heightSpec))  / 2
        super.onMeasure(widthSpec, heightSpec)
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        mDetector.onTouchEvent(e)
        return super.onTouchEvent(e)
    }

    override fun onScrolled(dx: Int, dy: Int) {
        super.onScrolled(dx, dy)
        val layoutManager = layoutManager
//        mLastVisibleItemPosition = (layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
//        mFirstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
    }

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)
        val layoutManager = layoutManager
        val visibleItemCount = layoutManager?.childCount ?: 0
        val totalItemCount = layoutManager?.itemCount ?: 0
        mAtLastPosition = visibleItemCount > 0 && mLastVisibleItemPosition == totalItemCount - 1
        mAtFirstPosition = mFirstVisibleItemPosition == 0
    }

    override fun dispatchNestedPreScroll(
        dx: Int,
        dy: Int,
        consumed: IntArray?,
        offsetInWindow: IntArray?,
        type: Int
    ): Boolean {
        val position = findCenterViewPosition()
        if (position != NO_POSITION && position != mLastCenterViewPosition) {
            mLastCenterViewPosition = position
            mPreScrollListener?.onPreScrollListener(dx, dy, position)
        }
        return super.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type)
    }


    private fun getPositionX(positionX: Float): Float {
        if (mCurrentScale < 1) {
            return 0f
        }
        val maxPositionX = mHalfWidth * (mCurrentScale - 1)
        return positionX.coerceIn(-maxPositionX, maxPositionX)
    }

    private fun getPositionY(positionY: Float): Float {
        if (mCurrentScale < 1) {
            return (mOriginalHeight / 2 - mHalfHeight).toFloat()
        }
        val maxPositionY = mHalfHeight * (mCurrentScale - 1)
        return positionY.coerceIn(-maxPositionY, maxPositionY)
    }

    private fun zoom(fromRate: Float, toRate: Float, fromX: Float, toX: Float, fromY: Float, toY: Float) {
        mIsZooming = true
        val animatorSet = AnimatorSet()
        val translationXAnimator = ValueAnimator.ofFloat(fromX, toX)
        translationXAnimator.addUpdateListener { animation -> x = animation.animatedValue as Float }

        val translationYAnimator = ValueAnimator.ofFloat(fromY, toY)
        translationYAnimator.addUpdateListener { animation -> y = animation.animatedValue as Float }

        val scaleAnimator = ValueAnimator.ofFloat(fromRate, toRate)
        scaleAnimator.addUpdateListener { animation ->
            mCurrentScale = animation.animatedValue as Float
            setScaleRate(mCurrentScale)
        }
        animatorSet.playTogether(translationXAnimator, translationYAnimator, scaleAnimator)
        animatorSet.duration = ANIMATOR_DURATION_TIME.toLong()
        animatorSet.interpolator = DecelerateInterpolator()
        animatorSet.start()
        animatorSet.doOnEnd {
            mIsZooming = false
            mCurrentScale = toRate
        }
    }

    private fun zoomScrollBy(dx: Int, dy: Int) {
        if (dx != 0) {
            x = getPositionX(x + dx)
        }
        if (dy != 0) {
            y = getPositionY(y + dy)
        }
    }

    private fun setScaleRate(rate: Float) {
        scaleX = rate
        scaleY = rate
    }

    fun onScale(scaleFactor: Float) {
        mCurrentScale *= scaleFactor
        mCurrentScale = mCurrentScale.coerceIn(MIN_SCALE_FACTOR, MAX_SCALE_FACTOR)
        setScaleRate(mCurrentScale)
        layoutParams.height = if (mCurrentScale < 1) { (mOriginalHeight / mCurrentScale).toInt() } else { mOriginalHeight }
        mHalfHeight = layoutParams.height / 2

        if (mCurrentScale != MIN_SCALE_FACTOR) {
            x = getPositionX(x)
            y = getPositionY(y)
        } else {
            x = 0f
            y = 0f
        }

        requestLayout()
    }

    fun onScaleBegin() { mDetector.isQuickScaling = mDetector.isDoubleTapping }

    fun onScaleEnd() {
        if (scaleX < MIN_SCALE_FACTOR) {
            zoom(mCurrentScale, MIN_SCALE_FACTOR, x, 0f, y, 0f)
        }
    }

    fun zoomFling(velocityX: Int, velocityY: Int): Boolean {
        if (mCurrentScale <= MIN_SCALE_FACTOR) return false

        val distanceTimeFactor = 0.4f
        val animatorSet = AnimatorSet()

        if (velocityX != 0) {
            val dx = (distanceTimeFactor * velocityX / 2)
            val newX = getPositionX(x + dx)
            val translationXAnimator = ValueAnimator.ofFloat(x, newX)
            translationXAnimator.addUpdateListener { animation -> x = getPositionX(animation.animatedValue as Float) }
            animatorSet.play(translationXAnimator)
        }
        if (velocityY != 0 && (mAtFirstPosition || mAtLastPosition)) {
            val dy = (distanceTimeFactor * velocityY / 2)
            val newY = getPositionY(y + dy)
            val translationYAnimator = ValueAnimator.ofFloat(y, newY)
            translationYAnimator.addUpdateListener { animation -> y = getPositionY(animation.animatedValue as Float) }
            animatorSet.play(translationYAnimator)
        }

        animatorSet.duration = BASE_ANIM_300L
        animatorSet.interpolator = DecelerateInterpolator()
        animatorSet.start()

        return true
    }

    fun setPreScrollListener(iComicPreScroll: IComicPreScroll) {
        mPreScrollListener = iComicPreScroll
    }
}