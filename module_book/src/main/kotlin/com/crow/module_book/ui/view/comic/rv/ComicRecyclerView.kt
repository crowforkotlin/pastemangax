package com.crow.module_book.ui.view.comic.rv

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.tools.extensions.findCenterViewPosition

/**
 * Implementation of a [RecyclerView] used by the webtoon reader.
 */
class ComicRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : RecyclerView(context, attrs, defStyle) {


    private var isZooming = false
    private var atLastPosition = false
    private var atFirstPosition = false
    private var firstVisibleItemPosition = 0
    private var lastVisibleItemPosition = 0
    private var mLastCenterViewPosition = 0

    private var mPreScrollListener: IComicPreScroll? = null
    private var mNestedPreScrollListener: IComicPreScroll? = null

    var tapListener: ((MotionEvent) -> Unit)? = null
    var longTapListener: ((MotionEvent) -> Boolean)? = null

    override fun onScrolled(dx: Int, dy: Int) {
        super.onScrolled(dx, dy)
        val layoutManager = layoutManager
        if (layoutManager is LinearLayoutManager) {
            lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
            firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
        }
    }

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)
        val layoutManager = layoutManager
        val visibleItemCount = layoutManager?.childCount ?: 0
        val totalItemCount = layoutManager?.itemCount ?: 0
        atLastPosition = visibleItemCount > 0 && lastVisibleItemPosition == totalItemCount - 1
        atFirstPosition = firstVisibleItemPosition == 0
    }

    override fun dispatchNestedPreScroll(
        dx: Int,
        dy: Int,
        consumed: IntArray?,
        offsetInWindow: IntArray?,
        type: Int
    ): Boolean {
        val position = findCenterViewPosition()
        if (position != NO_POSITION) {
            if (position != mLastCenterViewPosition) {
                mLastCenterViewPosition = position
                mPreScrollListener?.onPreScrollListener(dx, dy, position)
            }
            mNestedPreScrollListener?.onPreScrollListener(dx, dy, position)
        }
        return super.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type)
    }

    fun setPreScrollListener(iComicPreScroll: IComicPreScroll) {
        mPreScrollListener = iComicPreScroll
    }

    fun setNestedPreScrollListener(iComicPreScroll: IComicPreScroll) {
        mNestedPreScrollListener = iComicPreScroll
    }
}