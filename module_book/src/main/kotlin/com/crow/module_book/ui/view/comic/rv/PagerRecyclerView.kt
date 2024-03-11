package com.crow.module_book.ui.view.comic.rv

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.tools.extensions.findCenterViewPosition
import com.crow.module_book.ui.helper.GestureDetectorWithLongTap

class PagerRecyclerView : RecyclerView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    private inner class ComicRvGestureListener : GestureDetectorWithLongTap.Listener() {

        override fun onSingleTapConfirmed(ev: MotionEvent): Boolean {
            return false
        }

        override fun onDoubleTap(ev: MotionEvent): Boolean {
            return false
        }

        override fun onLongTapConfirmed(ev: MotionEvent) {

        }

        fun onDoubleTapConfirmed(ev: MotionEvent) {

        }
    }

    fun interface IComicPagerPreScroll { fun onPreScrollListener(dx: Int, dy: Int, position: Int) }

    private var mLastCenterViewPosition = 0

    private var mPreScrollListener: IComicPagerPreScroll? = null

    private var mNestedPreScrollListener: IComicPreScroll? = null

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
        mNestedPreScrollListener?.onPreScrollListener(dx, dy, position)
        return super.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type)
    }

    fun setPreScrollListener(iComicPreScroll: IComicPagerPreScroll) {
        mPreScrollListener = iComicPreScroll
    }
    fun setNestedPreScrollListener(iComicPreScroll: IComicPreScroll) {
        mNestedPreScrollListener = iComicPreScroll
    }
}