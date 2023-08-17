package com.crow.module_book.ui.view.comic

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import com.crow.base.tools.extensions.logMsg
import kotlin.math.roundToInt

class GestureHelper(context: Context, private val listener: GestureListener) :
    GestureDetector.SimpleOnGestureListener() {

    private val detector = GestureDetector(context, this)
    private val width = context.resources.displayMetrics.widthPixels
    private val height = context.resources.displayMetrics.heightPixels
    private var mIsDispatching = false

    init {
        detector.setIsLongpressEnabled(true)
        detector.setOnDoubleTapListener(this)
    }

    fun dispatchTouchEvent(event: MotionEvent, isDispatching: Boolean) {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) { mIsDispatching = isDispatching }
        detector.onTouchEvent(event)
    }

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        if (!mIsDispatching) return true
        val xIndex = (e.rawX * 2f / width).roundToInt()
        val yIndex = (e.rawY * 2f / height).roundToInt()
        "$xIndex\t\t$yIndex".logMsg()
        listener.onTouch(
            when (xIndex) {
                0 -> AREA_LEFT
                1 -> {
                    when (yIndex) {
                        0 -> AREA_TOP
                        1 -> AREA_CENTER
                        2 -> AREA_BOTTOM
                        else -> return false
                    }
                }
                2 -> AREA_RIGHT
                else -> return false
            },
        )
        return true
    }

    companion object {
        const val AREA_CENTER = 1
        const val AREA_LEFT = 2
        const val AREA_RIGHT = 3
        const val AREA_TOP = 4
        const val AREA_BOTTOM = 5
    }

    interface GestureListener {

        fun onTouch(area: Int)

    }
}