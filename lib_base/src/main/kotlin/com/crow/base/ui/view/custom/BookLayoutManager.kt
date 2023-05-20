package soko.ekibun.bangumi.plugins.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.tools.extensions.logMsg
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt


class BookLayoutManager(val context: Context, val updateContent: (View, BookLayoutManager) -> Unit) : LinearLayoutManager(context) {

    companion object {

        const val MAX_SCALE_FACTOR = 10f
        const val MILLISECONDS_PER_INCH = 100f
        const val MAX_SCROLL_ON_FLING_DURATION = 100 // ms
    }

    var mCurrentScale = 1f
        set(value) {
            field = max(1f, min(value, MAX_SCALE_FACTOR))
            findViewByPosition(downPage - 1)?.translationX = width * field
        }
    var offsetX = 0
    var offsetY = 0

    fun reset() {
        mCurrentScale = 1f
        offsetX = 0
        offsetY = 0
    }

    interface ScalableAdapter {
        fun isItemScalable(pos: Int, layoutManager: LinearLayoutManager): Boolean
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        if (orientation == VERTICAL) return super.onLayoutChildren(recycler, state)
        detachAndScrapAttachedViews(recycler)
        if (state.itemCount <= 0 || state.isPreLayout) return

        currentPos = Math.max(0f, Math.min(currentPos, state.itemCount - 1f))
        downPage = currentPos.toInt()

        val currentIndex = currentPos.toInt()
        val view = recycler.getViewForPosition(currentIndex)
        addView(view)
        measureChildWithMargins(view, 0, 0)
        view.translationZ = 50f
        view.translationX = -(currentPos - currentIndex) * width
        layoutDecoratedWithMargins(view, 0, 0, view.measuredWidth, view.measuredHeight)
        // 前一个
        if (currentIndex - 1 >= 0) {
            val nextView = recycler.getViewForPosition(currentIndex - 1)
            addView(nextView)
            nextView.translationX = -width * mCurrentScale
            nextView.translationZ = 100f
            measureChildWithMargins(nextView, 0, 0)
            layoutDecoratedWithMargins(nextView, 0, 0, view.measuredWidth, view.measuredHeight)
        }
        // 后一个
        if (currentIndex + 1 < state.itemCount) {
            val nextView = recycler.getViewForPosition(currentIndex + 1)
            addView(nextView)
            nextView.translationX = 0f
            nextView.translationZ = 0f
            measureChildWithMargins(nextView, 0, 0)
            layoutDecoratedWithMargins(nextView, 0, 0, view.measuredWidth, view.measuredHeight)
        }
    }

    fun scrollOnScale(x: Float, y: Float, oldScale: Float) {
        val adapter = recyclerView.adapter
        if (orientation == HORIZONTAL && adapter is ScalableAdapter && !adapter.isItemScalable(downPage, this)) mCurrentScale = 1f
        val anchorPos = (if (adapter is ScalableAdapter) {
            (findFirstVisibleItemPosition()..findLastVisibleItemPosition()).firstOrNull {
                adapter.isItemScalable(it, this)
            } ?: {
                mCurrentScale = 1f
                null
            }()
        } else findFirstVisibleItemPosition()) ?: return
        recyclerView.scrollBy(
            if (mCurrentScale == 1f && orientation == HORIZONTAL) 0 else ((offsetX + x) * (mCurrentScale - oldScale) / oldScale).toInt(),
            if (orientation == HORIZONTAL) ((offsetY + y) * (mCurrentScale - oldScale) / oldScale).toInt() else 0
        )
        if (orientation == VERTICAL) findViewByPosition(anchorPos)?.let {
            scrollToPositionWithOffset(anchorPos, (y - (-getDecoratedTop(it) + y) * mCurrentScale / oldScale).toInt())
        }
    }

    var currentPos = 0f
    override fun scrollToPositionWithOffset(position: Int, offset: Int) {
        currentPos = position.toFloat()
        super.scrollToPositionWithOffset(position, offset)
    }

    override fun measureChildWithMargins(child: View, widthUsed: Int, heightUsed: Int) {
        super.measureChildWithMargins(child, widthUsed, heightUsed)
        val lp = child.layoutParams as RecyclerView.LayoutParams
        val widthSpec = RecyclerView.LayoutManager.getChildMeasureSpec((width * mCurrentScale).toInt(), widthMode, paddingLeft + paddingRight + lp.leftMargin + lp.rightMargin + widthUsed, lp.width, canScrollHorizontally())
        val heightSpec = RecyclerView.LayoutManager.getChildMeasureSpec(height, heightMode, paddingTop + paddingBottom + lp.topMargin + lp.bottomMargin + heightUsed, lp.height, canScrollVertically())
        child.measure(widthSpec, heightSpec)
        if (orientation == VERTICAL || child.measuredHeight >= height) return
        child.measure(widthSpec, RecyclerView.LayoutManager.getChildMeasureSpec(height, heightMode, paddingTop + paddingBottom + lp.topMargin + lp.bottomMargin + heightUsed, RecyclerView.LayoutParams.MATCH_PARENT, canScrollVertically()))
    }

    /*
    * 用于为子视图设置布局位置和大小，并考虑到该子视图的 margin 值。该方法通常被用于自定义 LayoutManager 的实现中，用于确定子视图的布局和位置。
    * */
    override fun layoutDecoratedWithMargins(child: View, left: Int, top: Int, right: Int, bottom: Int) {
        updateContent(child, this)
        if (orientation == VERTICAL) child.translationZ = 0f
        offsetX = max(0, min(right - left - width, offsetX))
        offsetY = max(0, min(bottom - top - height, offsetY))
        super.layoutDecoratedWithMargins(child, left - offsetX, top - offsetY, right - offsetX, bottom - offsetY)
    }


    override fun scrollVerticallyBy(dy: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State?): Int {
        val view = findViewByPosition(downPage)
        val ddx = max(min(dy, (if (orientation == VERTICAL) (width * mCurrentScale).toInt() else view?.width ?: width) - width - offsetX), -offsetX)
        offsetX += ddx
        offsetChildrenHorizontal(-ddx)
        view?.translationX = 0f
        for (i in 0 until recyclerView.childCount) updateContent(recyclerView.getChildAt(i), this)

        if (orientation == VERTICAL || mCurrentScale > 1 || doingScale || view == null) return if (mCurrentScale == 1f) dy else ddx

        currentPos = Math.max(downPage - 1f, Math.min(currentPos + dy.toFloat() / width, downPage + 1f))
        currentPos = Math.max(0f, Math.min(currentPos, itemCount - 1f))
        view.translationX = -Math.max((currentPos - downPage) * width, 0f)
        if (currentPos < downPage) findViewByPosition(downPage - 1)?.translationX = -(currentPos - downPage + 1) * width
//        if(lastPos != currentPos.toInt())
        return dy
    }

    override fun scrollHorizontallyBy(dx: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State): Int {
        return  super.scrollHorizontallyBy(dx, recycler, state)
        val view = findViewByPosition(downPage)
        "dx : $dx\t\t value : ${width * mCurrentScale}\t\tscale : $mCurrentScale".logMsg()

        val ddx = max(min(dx, (if (orientation == VERTICAL) (width * mCurrentScale).toInt() else view?.width ?: width) - width - offsetX), -offsetX)
        offsetX += ddx
        offsetChildrenHorizontal(-ddx)
        view?.translationX = 0f
        for (i in 0 until recyclerView.childCount) updateContent(recyclerView.getChildAt(i), this)

        if (orientation == VERTICAL || mCurrentScale > 1 || doingScale || view == null) return if (mCurrentScale == 1f) dx else ddx

        currentPos = Math.max(downPage - 1f, Math.min(currentPos + dx.toFloat() / width, downPage + 1f))
        currentPos = Math.max(0f, Math.min(currentPos, itemCount - 1f))
        view.translationX = -Math.max((currentPos - downPage) * width, 0f)
        if (currentPos < downPage) findViewByPosition(downPage - 1)?.translationX = -(currentPos - downPage + 1) * width
//        if(lastPos != currentPos.toInt())
        return dx
    }

    override fun computeHorizontalScrollOffset(state: RecyclerView.State): Int {
        return if (orientation == VERTICAL) super.computeHorizontalScrollOffset(state)
        else (currentPos * width).toInt() + if (mCurrentScale > 1f) 1 else 0
    }

    override fun computeHorizontalScrollRange(state: RecyclerView.State): Int {
        return if (orientation == VERTICAL) super.computeHorizontalScrollRange(state)
        else itemCount * width
    }

    var downPage = 0

    override fun canScrollHorizontally(): Boolean = true
    override fun canScrollVertically(): Boolean = true

    var doingScale = false
    lateinit var recyclerView: RecyclerView

    @SuppressLint("ClickableViewAccessibility")
    fun setupWithRecyclerView(
        view: RecyclerView,
        onTap: (Int, Int) -> Unit,
        onPress: (View, Int) -> Unit,
        onTouch: (MotionEvent) -> Unit
    ) {
        recyclerView = view
        recyclerView.layoutManager = this
        var beginScale = mCurrentScale
        val scaleGestureDetector = ScaleGestureDetector(view.context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                    beginScale = mCurrentScale
                    currentPos = Math.round(currentPos).toFloat()
                    doingScale = true
                    requestLayout()
                    return super.onScaleBegin(detector)
                }

                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    val oldScale = mCurrentScale
                    mCurrentScale = beginScale * detector.scaleFactor
                    scrollOnScale(detector.focusX, detector.focusY, oldScale)
                    requestLayout()
                    return super.onScale(detector)
                }
            })
        val gestureDetector = GestureDetectorCompat(view.context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                onTap((e.x).toInt(), (e.y).toInt())
                return super.onSingleTapConfirmed(e)
            }

            override fun onLongPress(e: MotionEvent) {
                view.findChildViewUnder(e.x, e.y)?.let { onPress(it, view.getChildAdapterPosition(it)) }
                super.onLongPress(e)
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                val oldScale = mCurrentScale
                mCurrentScale = if (mCurrentScale < MAX_SCALE_FACTOR) MAX_SCALE_FACTOR else 1f
                scrollOnScale(e.x, e.y, oldScale)
                requestLayout()
                return super.onDoubleTap(e)
            }
        })
        recyclerView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (mCurrentScale * width < 10) mCurrentScale = 1f
                    doingScale = false
                    if (abs(currentPos - currentPos.roundToInt()) * width < 5) currentPos = Math.round(currentPos).toFloat()
                    requestLayout()
                }
            }

            onTouch(event)
            scaleGestureDetector.onTouchEvent(event)
            gestureDetector.onTouchEvent(event)
            false
        }
        recyclerView.onFlingListener = object : RecyclerView.OnFlingListener() {
            override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                val minFlingVelocity = recyclerView.minFlingVelocity
                if (orientation == VERTICAL || mCurrentScale > 1f) return false

                val targetPos = when {
                    Math.abs(velocityX) < minFlingVelocity -> Math.round(currentPos)
                    velocityX < 0 -> currentPos.toInt()
                    else -> Math.min(currentPos.toInt() + 1, itemCount - 1)
                }
                snapToTarget(targetPos)

                return true
            }
        }
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (orientation == VERTICAL || mCurrentScale > 1f) return
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    snapToTarget(Math.round(currentPos))
                }
            }
        })
    }

    fun snapToTarget(targetPos: Int) {
        if (targetPos < 0 || targetPos > itemCount - 1) return
        val smoothScroller: LinearSmoothScroller = createSnapScroller(targetPos)
        smoothScroller.targetPosition = targetPos
        startSmoothScroll(smoothScroller)
    }

    private fun createSnapScroller(targetPos: Int): LinearSmoothScroller {
        return object : LinearSmoothScroller(recyclerView.context) {
            override fun onTargetFound(targetView: View, state: RecyclerView.State, action: Action) {
                Log.v("snap", "$currentPos $targetPos")
                val dx = -((currentPos - targetPos) * (width + 0.5f)).toInt()
                val time = calculateTimeForDeceleration(Math.abs(dx))
                if (time > 0) {
                    action.update(dx, 0, time, mDecelerateInterpolator)
                }
            }

            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                return MILLISECONDS_PER_INCH / displayMetrics.densityDpi
            }

            override fun calculateTimeForScrolling(dx: Int): Int {
                return Math.min(
                    MAX_SCROLL_ON_FLING_DURATION,
                    super.calculateTimeForScrolling(dx)
                )
            }
        }
    }
}