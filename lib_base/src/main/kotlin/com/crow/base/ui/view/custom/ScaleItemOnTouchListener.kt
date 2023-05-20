package com.crow.base.ui.view.custom

import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.tools.extensions.BASE_ANIM_100L
import com.crow.base.ui.view.custom.ScaleItemOnTouchListener.Constants.DRAG_THRESHOLD
import com.crow.base.ui.view.custom.ScaleItemOnTouchListener.Constants.SCALE_DEFAULT
import com.crow.base.ui.view.custom.ScaleItemOnTouchListener.Constants.SCALE_UP
import kotlin.math.abs

class ScaleItemOnTouchListener : RecyclerView.OnItemTouchListener {

    private var previousX = 0f
    private var previousY = 0f

    private var previousMotionX = 0f
    private var previousMotionY = 0f

    private object Constants {
        const val SCALE_DEFAULT = 1f
        const val SCALE_UP = 1.4f
        const val DRAG_THRESHOLD = 1
    }

    override fun onTouchEvent(rv: RecyclerView, event: MotionEvent) {
        val childView = rv.findChildViewUnder(event.x, event.y)
        val previousChild = rv.findChildViewUnder(previousX, previousY)
        if (childView != null) {
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    if (childView.scaleX != SCALE_UP) {
                        scaleUp(childView)
                    }

                    if (previousChild != null && previousChild != childView) {
                        scaleDown(previousChild)
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    scaleDown(childView)
                    childView.callOnClick()
                }
            }
            previousX = childView.x
            previousY = childView.y
        } else if (previousChild != null && (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL)) {
            scaleDown(previousChild)
            previousX = 0f
            previousY = 0f
        }
    }

    override fun onInterceptTouchEvent(rv: RecyclerView, event: MotionEvent): Boolean {
        var interceptTouch = false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                previousMotionX = event.x
                previousMotionY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                interceptTouch = !(abs(event.x - previousMotionX) > DRAG_THRESHOLD || abs(event.y - previousMotionY) > DRAG_THRESHOLD)
            }
            MotionEvent.ACTION_UP -> {
                previousMotionX = 0f
                previousMotionY = 0f
            }
        }
        return interceptTouch
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
    }

    private fun scaleUp(view: View) {
        view.animate().setDuration(BASE_ANIM_100L).scaleX(SCALE_UP).scaleY(SCALE_UP).start()
    }

    private fun scaleDown(view: View) {
        view.animate().setDuration(BASE_ANIM_100L).scaleX(SCALE_DEFAULT).scaleY(SCALE_DEFAULT).start()
    }
}