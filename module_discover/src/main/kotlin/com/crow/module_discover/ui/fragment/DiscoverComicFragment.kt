package com.crow.module_discover.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.get
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.copymanga.BaseLoadStateAdapter
import com.crow.base.copymanga.BaseStrings
import com.crow.base.copymanga.entity.AppConfigEntity
import com.crow.base.copymanga.entity.Fragments
import com.crow.base.copymanga.glide.AppGlideProgressFactory
import com.crow.base.tools.coroutine.launchDelay
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.BASE_ANIM_300L
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.animateFadeOut
import com.crow.base.tools.extensions.animateFadeOutWithEndInVisibility
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.tools.extensions.doOnInterval
import com.crow.base.tools.extensions.findFisrtVisibleViewPosition
import com.crow.base.tools.extensions.immersionPadding
import com.crow.base.tools.extensions.navigateToWithBackStack
import com.crow.base.tools.extensions.repeatOnLifecycle
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.view.event.BaseEvent
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.base.ui.viewmodel.doOnSuccess
import com.crow.module_discover.R
import com.crow.module_discover.databinding.DiscoverFragmentComicBinding
import com.crow.module_discover.model.intent.DiscoverIntent
import com.crow.module_discover.ui.adapter.DiscoverComicAdapter
import com.crow.module_discover.ui.viewmodel.DiscoverViewModel
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.core.qualifier.named
import com.crow.base.R as baseR

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_discover/src/main/kotlin/com/crow/module_discover/ui/fragment
 * @Time: 2023/3/28 23:56
 * @Author: CrowForKotlin
 * @Description: DiscoverComicFragment
 * @formatter:on
 **************************/
class DiscoverComicFragment : BaseMviFragment<DiscoverFragmentComicBinding>() {

    companion object {

        private const val TRANSITION_VALUE_THRESHOLD = 60

        const val Comic = "Discover_Comic"

        fun newInstance() = DiscoverComicFragment() }

    /** ● (Activity级别) 发现页VM */
    private val mDiscoverVM by sharedViewModel<DiscoverViewModel>()

    /** ● 漫画适配器 */
    private lateinit var mDiscoverComicAdapter: DiscoverComicAdapter

    /**
     * ● RecyclerView 滚动处理
     *
     * ● 2023-09-09 01:26:38 周六 上午
     */
    private val mRvOnScroll = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            // 获取最后一个可见Child View
            val pos = (mBinding.discoverComicRv.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

            // onScrolled 在初始化添加给Rv时 Rv会第一次进行初始化
            if (mDiscoverVM.mVisiblePos == null) mDiscoverVM.mVisiblePos = pos
            else if (mDiscoverVM.mVisiblePos!! != pos) mDiscoverVM.mRvPos = pos
        }
    }

    /**
     * ● RecyclerView 状态处理
     *
     * ● 2023-09-09 01:26:20 周六 上午
     */
    private val mRvOnState = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                mBinding.discoverComicRv.removeOnScrollListener(this)
                mBinding.discoverComicRv.addOnScrollListener(mRvOnScroll)
            }
        }
    }

    /** ● 收集状态 */
    fun onCollectState() {
        repeatOnLifecycle {
            mDiscoverVM.mDiscoverComicHomeFlowPager?.collect {
                mDiscoverComicAdapter.submitData(it)
            }
        }
    }

    /** ● 获取ViewBinding */
    override fun getViewBinding(inflater: LayoutInflater) = DiscoverFragmentComicBinding.inflate(inflater)

    /** ● 导航至漫画页 */
    private fun navigateBookComicInfo(pathword: String) {
        val bundle = Bundle()
        bundle.putSerializable(BaseStrings.PATH_WORD, pathword)
        requireParentFragment().parentFragmentManager.navigateToWithBackStack(baseR.id.app_main_fcv,
            requireActivity().supportFragmentManager.findFragmentByTag(Fragments.Container.name)!!,
            get<Fragment>(named(Fragments.BookComicInfo.name)).also { it.arguments = bundle }, Fragments.BookComicInfo.name, Fragments.BookComicInfo.name
        )
    }

    /**
     * ● 处理滚动RV
     *
     * ● 2023-09-09 01:16:52 周六 上午
     */
    private fun onInterceptScrollRv(pos: Int) {
        when {
            pos == 0 -> {
                if (mDiscoverVM.mRvPos > TRANSITION_VALUE_THRESHOLD) onProcessScroll(pos)
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
        mBinding.discoverComicRv.removeOnScrollListener(mRvOnScroll)
        mBinding.discoverComicRv.addOnScrollListener(mRvOnState)
        mBinding.discoverComicRv.smoothScrollToPosition(pos)
    }

    /**
     * ● 带有过渡效果的滚动
     *
     * ● 2023-09-09 01:17:11 周六 上午
     */
    private fun onProcessScroll(pos: Int) {
        mBinding.discoverComicRv.animateFadeOut().withEndAction {
            mBinding.discoverComicRv.scrollToPosition(pos)
            mBinding.discoverComicRv.animateFadeIn()
        }
    }

    /** ● 初始化监听事件 */
    override fun initListener() {

        parentFragmentManager.setFragmentResultListener("onDoubleTap", this) { _, _ ->
            BaseEvent.getSIngleInstance().doOnInterval {
                if (mBinding.discoverComicRv.findFisrtVisibleViewPosition() > 0) {
                   onInterceptScrollRv(0)
                } else {
                   onInterceptScrollRv(mDiscoverVM.mRvPos)
                }
            }
        }


        // 设置容器Fragment的回调监听r
        parentFragmentManager.setFragmentResultListener(Comic, this) { _, bundle ->
            if (bundle.getInt(BaseStrings.ID) == 1) {
                mBinding.discoverComicRefresh.autoRefreshAnimationOnly()
                mBinding.discoverComicRefresh.finishRefresh((BASE_ANIM_300L.toInt() shl 1) or 0xFF)
                if (bundle.getBoolean(BaseStrings.ENABLE_DELAY)) {
                    launchDelay(BASE_ANIM_200L) {
                        onCollectState()
                    }
                } else {
                    onCollectState()
                }
            }
        }

        // 刷新监听
        mBinding.discoverComicRefresh.setOnRefreshListener { mDiscoverComicAdapter.refresh() }

        // 更多选项 点击监听
        mBinding.discoverComicAppbar.discoverAppbarToolbar.menu[0].doOnClickInterval { toast("此功能或许将在下个版本中完善....") }

        // 滑动监听
        mBinding.discoverComicRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (AppConfigEntity.getInstance().mAppFirstInit)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    toast("${(recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()} / ${recyclerView.adapter?.itemCount}")
                }
            }
        })
    }

    /** ● 初始化视图 */
    override fun initView(savedInstanceState: Bundle?) {

        // 设置 内边距属性 实现沉浸式效果
        immersionPadding(mBinding.discoverComicAppbar.root, paddingNaviateBar = false)

        // 设置Title
        mBinding.discoverComicAppbar.discoverAppbarToolbar.title = getString(R.string.discover_comic)

        // 初始化 发现页 漫画适配器
        mDiscoverComicAdapter = DiscoverComicAdapter { navigateBookComicInfo(it.mPathWord) }

        // 设置适配器
        mBinding.discoverComicRv.adapter = mDiscoverComicAdapter.withLoadStateFooter(BaseLoadStateAdapter { mDiscoverComicAdapter.retry() })

        // 设置加载动画独占1行，漫画卡片3行
        (mBinding.discoverComicRv.layoutManager as GridLayoutManager).apply {
            spanSizeLookup = object : SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (position == mDiscoverComicAdapter.itemCount  && mDiscoverComicAdapter.itemCount > 0) 3
                    else 1
                }
            }
        }
    }

    /** ● 初始化观察者 */
    override fun initObserver(saveInstanceState: Bundle?) {

        // 意图观察者
        mDiscoverVM.onOutput { intent ->
            when(intent) {
                is DiscoverIntent.GetComicHome -> {
                    intent.mBaseViewState
                        .doOnSuccess { if (mBinding.discoverComicRefresh.isRefreshing) mBinding.discoverComicRefresh.finishRefresh() }
                        .doOnError { _, _ ->
                            if (mDiscoverComicAdapter.itemCount == 0) {

                                // 错误提示淡入
                                mBinding.discoverComicTipsError.animateFadeIn()

                                // 发现页 “漫画” 淡出
                                mBinding.discoverComicRv.animateFadeOutWithEndInVisibility()
                            }

                            if (mBinding.discoverComicTipsError.isGone) toast(getString(baseR.string.BaseLoadingErrorNeedRefresh))
                        }
                        .doOnResult {
                            // 错误提示 可见
                            if (mBinding.discoverComicTipsError.isVisible) {
                                mBinding.discoverComicTipsError.isVisible = false
                                mBinding.discoverComicRv.animateFadeIn()
                            }
                        }
                }
                else -> {}
            }
        }
    }

    /** ● Lifecycle onCreate */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (mDiscoverVM.mComicHomeData != null) return
        mDiscoverVM.input(DiscoverIntent.GetComicTag())     // 获取标签
        mDiscoverVM.input(DiscoverIntent.GetComicHome())    // 获取发现主页
    }

    /**
     * ● Lifecycle OnViewCreated
     *
     * ● 2023-09-09 01:37:34 周六 上午
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // 在onViewCreate 中装载事件 确保可初始化预加载可见的 Child View的个数
        mBinding.discoverComicRv.addOnScrollListener(mRvOnScroll)

        super.onViewCreated(view, savedInstanceState)

    }

    /** ● Lifecycle onDestroyView */
    override fun onDestroyView() {
        super.onDestroyView()
        AppGlideProgressFactory.doReset()
        parentFragmentManager.clearFragmentResultListener(Comic)
    }
}