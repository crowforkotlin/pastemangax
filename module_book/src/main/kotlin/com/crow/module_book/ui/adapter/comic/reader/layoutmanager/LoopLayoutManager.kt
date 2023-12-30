package com.crow.module_book.ui.adapter.comic.reader.layoutmanager

import android.content.Context
import android.graphics.PointF
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import kotlin.math.abs

class LoopLayoutManager(
        private val context: Context
) : RecyclerView.LayoutManager(), RecyclerView.SmoothScroller.ScrollVectorProvider {

    companion object {
        private const val START = 0
        private const val END = 1
    }

    @JvmField
    internal var mOrientationHelper: OrientationHelper? = null
    private val mOnPageChangeListeners = ArrayList<OnPageChangeListener>()
    private var mSmoothScroller: RecyclerView.SmoothScroller? = null
    private var mRequestLayout = true
    private var mFirstLayout = true
    private var mRecyclerView: RecyclerView? = null
    internal var mIsLoop: Boolean = false
    internal var mCurrentItem: Int = -1

    override fun onAttachedToWindow(view: RecyclerView) {
        super.onAttachedToWindow(view)
        if (mRecyclerView != view) {
            mRecyclerView = view
            snapHelper.attachToRecyclerView(view)
            view.addOnScrollListener(object : RecyclerView.OnScrollListener(){
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    handleScrollStateChanged(recyclerView, newState)
                }
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) { }
            })
        }
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        val itemCount = state.itemCount
        if (itemCount <= 0) {
            removeAndRecycleAllViews(recycler)
            return
        }
        if (state.isPreLayout) return
        if (!state.didStructureChange() && !mRequestLayout) return

        detachAndScrapAttachedViews(recycler)
        val orientationHelper = getOrientationHelper()
        val parentEnd = orientationHelper.endAfterPadding
        var childStart = 0
        val initPos = if (mCurrentItem in 0 until itemCount) mCurrentItem else 0
        for (i in initPos until itemCount) {
            val v = recycler.getViewForPosition(i)
            addView(v)
            measureChildWithMargins(v, 0, 0)
            val childW = getDecoratedMeasuredWidth(v)
            val childH = getDecoratedMeasuredHeight(v)
            layoutDecoratedWithMargins(v, childStart, 0, childStart + childW, childH)

            childStart += childW
            if (childStart > parentEnd) {
                break
            }
        }

        if (mRequestLayout || mFirstLayout) {
            mCurrentItem = initPos
        }
        mRequestLayout = false

        if (mFirstLayout) {
            mFirstLayout = false
            dispatchOnPageSelected(initPos)
        }
    }

    private fun normalizedPos(position: Int): Int {
        val itemCount = itemCount
        return if (canLoop()) {
            position % itemCount
        } else {
            if (position >= itemCount) itemCount - 1 else position
        }
    }

    // scroll methods start
    override fun scrollToPosition(position: Int) {
        mCurrentItem = normalizedPos(position)
        mRequestLayout = true
        requestLayout()
        dispatchOnPageSelected(mCurrentItem)
    }

    override fun smoothScrollToPosition(recyclerView: RecyclerView, state: RecyclerView.State?, position: Int) {
        val scroller = LinearSmoothScroller(recyclerView.context)
        scroller.targetPosition = normalizedPos(position)
        startSmoothScroll(scroller)
        mSmoothScroller = scroller
        mCurrentItem = scroller.targetPosition
        dispatchOnPageSelected(mCurrentItem)
    }

    override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
        val itemCount = itemCount
        if (childCount == 0 || itemCount == 0) {
            return null
        }

        val scroller = mSmoothScroller
        val firstChildPos = getPosition(getChildAt(0)!!)
        if (canLoop() && scroller != null && (scroller.isRunning || scroller.isPendingInitialRun)) {
            val distancePos = targetPosition - firstChildPos
            val direction = if (targetPosition < firstChildPos) {
                // start
                if (itemCount > 2 * abs(distancePos)) START else END
            } else {
                // end
                if (itemCount > 2 * abs(distancePos)) END else START
            }
            return PointF(direction.toFloat(), 0f)
        }
        val direction = if (targetPosition < firstChildPos) START else END
        return PointF(direction.toFloat(), 0f)
    }

    override fun canScrollHorizontally(): Boolean {
        return true
    }

    override fun scrollHorizontallyBy(dx: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State): Int {
        if (dx == 0) return 0
        val consumedDx = fill(dx, recycler, state)
        if (consumedDx == 0) {
            // 没有消耗dx，说明已经无法移动了，到达边界或者是其它情况
            return 0
        }
        offsetChildrenHorizontal(-consumedDx)
        // 回收不可见的view
        recycleViews(dx, recycler, state)
        return consumedDx
    }

    internal val snapHelper: SnapHelper = object : PagerSnapHelper() {
        override fun findTargetSnapPosition(layoutManager: RecyclerView.LayoutManager, velocityX: Int, velocityY: Int): Int {
            val itemCount = layoutManager.itemCount
            val pos = super.findTargetSnapPosition(layoutManager, velocityX, velocityY)
//            android.util.Log.d(TAG, "target snap pos: $pos, itemCount: $itemCount")
            if (pos >= itemCount) {
                return 0
            }
            return pos
        }
    }
    // scroll methods end

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT)
    }

    override fun checkLayoutParams(lp: RecyclerView.LayoutParams?): Boolean {
        return lp is RecyclerView.LayoutParams
    }

    fun addPageChangeListener(l: OnPageChangeListener) {
        if (!mOnPageChangeListeners.contains(l)) {
            mOnPageChangeListeners.add(l)
        }
    }

    fun removePageChangeListener(l: OnPageChangeListener) {
        mOnPageChangeListeners.remove(l)
    }

    internal fun dispatchOnPageSelected(pos: Int) {
        for (l in mOnPageChangeListeners) {
            l.onPageSelected(pos)
        }
    }

    internal fun dispatchOnPageScrollStateChanged(state: Int) {
        for (l in mOnPageChangeListeners) {
            l.onPageScrollState(state)
        }
    }
}