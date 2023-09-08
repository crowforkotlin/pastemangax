package com.crow.base.copymanga.ui.view

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
     * ● RecyclerView Position
     *
     * ● 2023-09-08 22:46:26 周五 下午
     */
    var mRvPos: Int = 0
        private set

    /**
     * ● RecyclerView 可见的ChildView 个数
     *
     * ● 2023-09-09 01:53:44 周六 上午
     */
    var mVisiblePos: Int? = null
        private set

    /**
     * ● RecyclerView 滚动处理
     *
     * ● 2023-09-09 01:26:38 周六 上午
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
     * ● RecyclerView 状态处理
     *
     * ● 2023-09-09 01:26:20 周六 上午
     */
    private val mRvOnState = object : OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == SCROLL_STATE_IDLE) {
                removeOnScrollListener(this)
                addOnScrollListener(mRvOnScroll)
            }
        }
    }

    init {
        addOnScrollListener(mRvOnScroll)
    }

    /**
     * ● 处理滚动RV
     *
     * ● 2023-09-09 01:16:52 周六 上午
     */
    fun onInterceptScrollRv(pos: Int) {
        when {
            pos == 0 -> {
                if (mRvPos > TRANSITION_VALUE_THRESHOLD) onProcessScroll(pos)
                else onProcessSmoothScroll(pos)
            }
            pos > TRANSITION_VALUE_THRESHOLD -> onProcessScroll(pos)
            else -> onProcessSmoothScroll(pos)
        }
    }

    /**
     * ● 平滑滚动
     *
     * ● 2023-09-09 01:17:03 周六 上午
     */
    private fun onProcessSmoothScroll(pos: Int) {
        removeOnScrollListener(mRvOnScroll)
        addOnScrollListener(mRvOnState)
        smoothScrollToPosition(pos)
    }

    /**
     * ● 带有过渡效果的滚动
     *
     * ● 2023-09-09 01:17:11 周六 上午
     */
    private fun onProcessScroll(pos: Int) {
        animateFadeOut().withEndAction {
            scrollToPosition(pos)
            animateFadeIn()
        }
    }

}