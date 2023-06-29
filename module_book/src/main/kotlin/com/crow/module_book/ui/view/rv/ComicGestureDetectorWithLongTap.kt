package com.crow.module_book.ui.view.rv

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewConfiguration
import kotlin.math.abs

/**
 * A custom gesture detector that also implements an on long tap confirmed, because the built-in
 * one conflicts with the quick scale feature.
 */
open class ComicGestureDetectorWithLongTap  (
    mContext: Context,
    mGestureComicGestureListener: ComicGestureListener,
) : GestureDetector(mContext, mGestureComicGestureListener) {

    /**
     * Custom listener to also include a long tap confirmed
     */
    open class ComicGestureListener : SimpleOnGestureListener() {

        /**
         * Notified when a long tap occurs with the initial on down [ev] that triggered it.
         */
        open fun onLongTapConfirmed(ev: MotionEvent) {}
    }

    private val mHandler = Handler(Looper.getMainLooper())
    private val mSlop = ViewConfiguration.get(mContext).scaledTouchSlop
    private val mLongTapTime = ViewConfiguration.getLongPressTimeout().toLong()
    private val mDoubleTapTime = ViewConfiguration.getDoubleTapTimeout().toLong()

    private var mDownX = 0f
    private var mDownY = 0f
    private var mLastUp = 0L
    private var mLastDownEvent: MotionEvent? = null

    /**
     * Runnable to execute when a long tap is confirmed.
     */
    private val mLongTapRunnable = Runnable { mGestureComicGestureListener.onLongTapConfirmed(mLastDownEvent!!) }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                mLastDownEvent?.recycle()
                mLastDownEvent = MotionEvent.obtain(ev)

                /**
                 * This is the key difference with the built-in detector. We have to ignore the
                 * event if the last up and current down are too close in time (double tap).
                 */
                if (ev.downTime - mLastUp > mDoubleTapTime) {
                    mDownX = ev.rawX
                    mDownY = ev.rawY
                    mHandler.postDelayed(mLongTapRunnable, mLongTapTime)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (abs(ev.rawX - mDownX) > mSlop || abs(ev.rawY - mDownY) > mSlop) {
                    mHandler.removeCallbacks(mLongTapRunnable)
                }
            }
            MotionEvent.ACTION_UP -> {
                mLastUp = ev.eventTime
                mHandler.removeCallbacks(mLongTapRunnable)
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_POINTER_DOWN -> {
                mHandler.removeCallbacks(mLongTapRunnable)
            }
        }
        return super.onTouchEvent(ev)
    }
}
