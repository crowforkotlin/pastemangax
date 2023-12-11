package com.crow.module_book.ui.view.comic.rv

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.FrameLayout

/**
 * Frame layout which contains a [ComicRecyclerView]. It's needed to handle touch events,
 * because the recyclerview is scaled and its touch events are translated, which breaks the
 * detectors.
 *
 * 非常感谢此开源库提供的代码 ：[https://github.com/tachiyomiorg/tachiyomi/blob/master/app/src/main/java/eu/kanade/tachiyomi/ui/reader/viewer/webtoon/WebtoonFrame.kt]
 */
class ComicFrameLayout  : FrameLayout {

    companion object {
        var mIsScanning = false
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    /**
     * Scale listener used to delegate events to the recycler view.
     */
    private inner class ComicScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            mIsScanning = true
            mChildRv.onScaleBegin()
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            mChildRv.onScale(detector.scaleFactor)
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            mChildRv.onScaleEnd()
        }
    }

    /**
     * Fling listener used to delegate events to the recycler view.
     */
    private inner class ComicFlingListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            return mChildRv.zoomFling(velocityX.toInt(), velocityY.toInt())
        }
    }
    
    /**
     * Scale detector, either with pinch or quick scale.
     * 比例手势检测器 --> 缩放
     */
    private val mScaleDetector = ScaleGestureDetector(context, ComicScaleListener())

    /**
     * Fling detector.
     */
    private val mFlingDetector = GestureDetector(context, ComicFlingListener())

    /**
     * Recycler view added in this frame.
     * 将子Rv添加至 该FrameLayout 下
     */
    private val mChildRv: ComicRecyclerView get() = (getChildAt(0) as? ComicRecyclerView)!!

    var mDoubleTapZoom = true
        set(value) {
            field = value
            mChildRv.mDoubleTapZoom = value
            mScaleDetector.isQuickScaleEnabled = value
        }
    
    /**
     * Dispatches a touch event to the detectors.
     * 向检测器发送触摸事件 并且 交给子View去处理事件 这里指ComicRecyclerView
     */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        mScaleDetector.onTouchEvent(ev)
        mFlingDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }
}
