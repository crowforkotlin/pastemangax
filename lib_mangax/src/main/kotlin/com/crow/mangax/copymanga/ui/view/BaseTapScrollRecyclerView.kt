package com.crow.mangax.copymanga.ui.view

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.animateFadeOut

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Package: com.crow.base.copymanga.ui.view
 * @Time: 2023/9/9 1:59
 * @Author: CrowForKotlin
 * @Description:
 * @formatter:on
 **************************/
class BaseTapScrollRecyclerView : RecyclerView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    companion object {
        private const val TRANSITION_VALUE_THRESHOLD = 60
    }

    /**
     * ⦁ RecyclerView Position
     *
     * ⦁ 2023-09-08 22:46:26 周五 下午
     */
    var mRvPos: Int = 0
        private set

    /**
     * ⦁ RecyclerView 可见的ChildView 个数
     *
     * ⦁ 2023-09-09 01:53:44 周六 上午
     */
    var mVisiblePos: Int? = null
        private set

    /**
     * ⦁ 是否正在滑动？
     *
     * ⦁ 2023-09-10 20:35:37 周日 下午
     */
    private var mIsScrolling = false

    /**
     * ⦁ RecyclerView 滚动处理
     *
     * ⦁ 2023-09-09 01:26:38 周六 上午
     */
    private val mRvOnScroll = object : OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            // 获取最后一个可见Child View
            val pos = (layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

            // onScrolled 在初始化添加给Rv时 Rv会第一次进行初始化
            if (mVisiblePos == null) mVisiblePos = pos
            else if (mVisiblePos!! != pos) mRvPos = pos

        }
    }

    /**
     * ⦁ RecyclerView 状态处理
     *
     * ⦁ 2023-09-09 01:26:20 周六 上午
     */
    private val mRvOnScrollState = object : OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == SCROLL_STATE_IDLE) {
                removeOnScrollListener(this)
                addOnScrollListener(mRvOnScroll)
                mIsScrolling = false
            }
        }
    }

    init {
        addOnScrollListener(mRvOnScroll)
    }

    /**
     * ⦁ 处理滚动RV
     *
     * ⦁ 2023-09-09 01:16:52 周六 上午
     *
     * @param toPosition 目标位置
     * @param precisePosition 精准的实际位置
     */
    fun onInterceptScrollRv(toPosition: Int = mRvPos, precisePosition: Int) {
        if (toPosition < 0) return
        if (toPosition == precisePosition) return
        when {
            toPosition == 0 -> {
                if (mRvPos > TRANSITION_VALUE_THRESHOLD) onProcessScroll(toPosition)
                else onProcessSmoothScroll(toPosition, precisePosition)
            }
            toPosition > TRANSITION_VALUE_THRESHOLD -> onProcessScroll(toPosition)
            else -> onProcessSmoothScroll(toPosition, precisePosition)
        }
    }

    /**
     * ⦁ 平滑滚动
     *
     * ⦁ 2023-09-09 01:17:03 周六 上午
     */
    private fun onProcessSmoothScroll(toPosition: Int, precisePosition: Int) {

        /*
        * 可能在处理平滑滚动的时候 会手动继续 让平滑滚动处理下去导致 无法对SCROLL STATE进行IDLE(终止)捕获
        * 所以当下次事件到来 时 需要做 判断是否大于 临界值，是的话则直接定位到实际位置 并让SCROLL STATE 的IDLE捕获到 从而恢复
        * 滚动的POS监听处理
        * */

        if (precisePosition > TRANSITION_VALUE_THRESHOLD) {
            onProcessScroll(toPosition)
            return
        }
        if (mIsScrolling) return
        mIsScrolling = true
        removeOnScrollListener(mRvOnScroll)
        addOnScrollListener(mRvOnScrollState)
        smoothScrollToPosition(toPosition)

    }

    /**
     * ⦁ 带有过渡效果的滚动
     *
     * ⦁ 2023-09-09 01:17:11 周六 上午
     */
    private fun onProcessScroll(pos: Int) {
        animateFadeOut().withEndAction {
            scrollToPosition(pos)
            animateFadeIn()
        }
    }

}