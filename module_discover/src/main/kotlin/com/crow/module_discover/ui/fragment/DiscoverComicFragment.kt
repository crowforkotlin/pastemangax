package com.crow.module_discover.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.core.view.get
import androidx.core.view.isEmpty
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import com.crow.base.app.app
import com.crow.base.copymanga.BaseLoadStateAdapter
import com.crow.base.copymanga.BaseStrings
import com.crow.base.copymanga.entity.Fragments
import com.crow.base.copymanga.glide.AppGlideProgressFactory
import com.crow.base.tools.coroutine.launchDelay
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.BASE_ANIM_300L
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.animateFadeOutWithEndInVisibility
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.tools.extensions.findFisrtVisibleViewPosition
import com.crow.base.tools.extensions.navigateToWithBackStack
import com.crow.base.tools.extensions.newMaterialDialog
import com.crow.base.tools.extensions.px2sp
import com.crow.base.tools.extensions.repeatOnLifecycle
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.view.event.BaseEvent
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.base.ui.viewmodel.doOnSuccess
import com.crow.module_discover.R
import com.crow.module_discover.databinding.DiscoverComicMoreLayoutBinding
import com.crow.module_discover.databinding.DiscoverFragmentComicBinding
import com.crow.module_discover.model.intent.DiscoverIntent
import com.crow.module_discover.model.resp.comic_tag.Theme
import com.crow.module_discover.model.resp.comic_tag.Top
import com.crow.module_discover.ui.adapter.DiscoverComicAdapter
import com.crow.module_discover.ui.viewmodel.DiscoverViewModel
import com.google.android.material.chip.Chip
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.core.qualifier.named
import kotlin.properties.Delegates
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


        const val COMIC = "Discover_Comic"

        fun newInstance() = DiscoverComicFragment()
    }

    /** ● (Activity级别) 发现页VM */
    private val mVM by sharedViewModel<DiscoverViewModel>()

    /** ● 漫画适配器 */
    private lateinit var mDiscoverComicAdapter: DiscoverComicAdapter

    /**
     * ● subtitle textview
     *
     * ● 2023-10-01 21:59:31 周日 下午
     */
    private var mToolbarSubtitle: TextView? = null

    private var  mSubtitlePrefix: String by Delegates.observable(app.getString(R.string.discover_all)) { _, _, new ->
        mBinding.discoverComicAppbar.discoverAppbarToolbar.subtitle = getString(R.string.discover_subtitle, new, mSubtitleSuffix)
    }

    private var  mSubtitleSuffix: String by Delegates.observable(app.getString(R.string.discover_all)) { _, _, new ->
        mBinding.discoverComicAppbar.discoverAppbarToolbar.subtitle = getString(R.string.discover_subtitle, mSubtitlePrefix, new)
    }

    /** ● 收集状态 */
    fun onCollectState() {
        if (mVM.mTotals == 0 && mVM.mDiscoverComicHomeFlowPager == null) {
            mVM.input(DiscoverIntent.GetComicTag()) // 获取标签
            mVM.input(DiscoverIntent.GetComicHome())    // 获取发现主页
        }
        repeatOnLifecycle {
            mVM.mDiscoverComicHomeFlowPager?.collect {
                mDiscoverComicAdapter.submitData(it)
            }
        }
    }

    /** ● 获取ViewBinding */
    override fun getViewBinding(inflater: LayoutInflater) = DiscoverFragmentComicBinding.inflate(inflater)

    /** ● 导航至漫画页 */
    private fun navigateBookComicInfo(name: String, pathword: String) {
        val tag = Fragments.BookComicInfo.name
        val bundle = Bundle()
        bundle.putSerializable(BaseStrings.PATH_WORD, pathword)
        bundle.putSerializable(BaseStrings.NAME, name)
        requireParentFragment().parentFragmentManager.navigateToWithBackStack(
            id = baseR.id.app_main_fcv,
            hideTarget = requireActivity().supportFragmentManager.findFragmentByTag(Fragments.Container.name)!!,
            addedTarget = get<Fragment>(named(tag)).also { it.arguments = bundle },
            tag = tag,
            backStackName = tag
        )
    }

    /** ● 初始化监听事件 */
    override fun initListener() {

        // 处理双击事件
        parentFragmentManager.setFragmentResultListener("onDoubleTap_Discover_Comic", this) { _, _ ->
            val first = mBinding.discoverComicRv.findFisrtVisibleViewPosition()
            if (first > 0) {
                mBinding.discoverComicRv.onInterceptScrollRv(toPosition = 0, precisePosition = first)
            } else {
                mBinding.discoverComicRv.onInterceptScrollRv(precisePosition = first)
            }
        }

        // 设置容器Fragment的回调监听
        parentFragmentManager.setFragmentResultListener(COMIC, this) { _, bundle ->
            if (bundle.getInt(BaseStrings.ID) == 1) {
                mBinding.discoverComicRefresh.autoRefreshAnimationOnly()
                mBinding.discoverComicRefresh.finishRefresh((BASE_ANIM_300L.toInt() shl 1) or 0xFF)
                if (bundle.getBoolean(BaseStrings.ENABLE_DELAY)) {
                    launchDelay(BASE_ANIM_200L) { onCollectState() }
                } else {
                    onCollectState()
                }
            }
        }

        // 刷新监听
        mBinding.discoverComicRefresh.setOnRefreshListener { mDiscoverComicAdapter.refresh() }

        // 漫画加载状态监听
        mDiscoverComicAdapter.addLoadStateListener {
            if(it.source.refresh is LoadState.NotLoading) {

                val toolbar = mBinding.discoverComicAppbar.discoverAppbarToolbar

                if (toolbar.menu.isEmpty()) {

                    // 先清空
                    toolbar.menu.clear()

                    // 加载布局
                    toolbar.inflateMenu(R.menu.discover_appbar_menu)

                    // 类别
                    toolbar.menu[0].doOnClickInterval { onSelectMenu(R.string.discover_tag) }

                    // 地区
                    toolbar.menu[1].doOnClickInterval { onSelectMenu(R.string.discover_location) }

                    // 更新时间

                    val instance = BaseEvent.getSIngleInstance()
                    toolbar.menu[2].doOnClickInterval {
                        if (instance.getBoolean("DISCOVER_COMIC_FRAGMENT_UPDATE_ORDER") == true) {
                            instance.setBoolean("DISCOVER_COMIC_FRAGMENT_UPDATE_ORDER", false)
                            mVM.setOrder("-datetime_updated")
                        } else {
                            instance.setBoolean("DISCOVER_COMIC_FRAGMENT_UPDATE_ORDER", true)
                            mVM.setOrder("datetime_updated")
                        }
                        updateComic()
                    }

                    // 热度
                    toolbar.menu[3].doOnClickInterval {
                        if (instance.getBoolean("DISCOVER_COMIC_FRAGMENT_POPULAR_ORDER") == true) {
                            instance.setBoolean("DISCOVER_COMIC_FRAGMENT_POPULAR_ORDER", false)
                            mVM.setOrder("-popular")
                        } else {
                            instance.setBoolean("DISCOVER_COMIC_FRAGMENT_POPULAR_ORDER", true)
                            mVM.setOrder("popular")
                        }
                        updateComic()
                    }

                    if (toolbar.subtitle.isNullOrEmpty()) {
                        mSubtitlePrefix = getString(R.string.discover_all)
                    }

                    // subtitle textview
                    mToolbarSubtitle = toolbar::class.java.superclass.getDeclaredField("mSubtitleTextView").run {
                        isAccessible = true
                        get(toolbar) as TextView
                    }
                }
            }
        }
    }

    /**
     * ● 更新漫画
     *
     * ● 2023-10-01 22:30:53 周日 下午
     */
    private fun updateComic() {
        viewLifecycleOwner.lifecycleScope.launch {
            mDiscoverComicAdapter.submitData(PagingData.empty())
            mVM.input(DiscoverIntent.GetComicHome())
            onCollectState()
        }
    }


    private fun onSelectMenu(type: Int) {

        if (mVM.mComicTagResp == null) {
            mVM.input(DiscoverIntent.GetComicTag(showDialog = true, type = type))
            return
        }
        mBinding.discoverComicRv.stopScroll()

        val binding = DiscoverComicMoreLayoutBinding.inflate(layoutInflater)
        val chipTextSize = mContext.px2sp(resources.getDimension(baseR.dimen.base_sp12_5))
        var job: Job? = null
        val dialog = mContext.newMaterialDialog {
            it.setTitle(getString(type))
            it.setView(binding.root)
            it.setOnDismissListener { job?.cancel() }
        }
        when(type) {
            R.string.discover_tag -> {
                job = viewLifecycleOwner.lifecycleScope.launch {
                    var theme  = mVM.mComicTagResp!!.theme.toMutableList()
                    if (theme.size > 25) { theme = theme.subList(0, 25) }
                    theme.add(0, Theme(null, 0, 0, null, getString(R.string.discover_all), ""))
                    theme.forEach {
                        val chip = Chip(mContext)
                        chip.text = it.mName
                        chip.textSize = chipTextSize
                        chip.doOnClickInterval { _ ->
                            dialog.cancel()
                            mSubtitlePrefix = it.mName
                            mToolbarSubtitle?.animateFadeIn()
                            mVM.setTheme(it.mPathWord)
                            updateComic()
                        }
                        binding.moreChipGroup.addView(chip)
                        delay(16L)
                    }
                }
            }
            R.string.discover_location ->{
                job = viewLifecycleOwner.lifecycleScope.launch {
                    var top = mVM.mComicTagResp!!.top.toMutableList()
                    if (top.size > 25) { top = top.subList(0, 25) }
                    top.add(0, Top(getString(R.string.discover_all), ""))
                    top.forEach {
                        val chip = Chip(mContext)
                        chip.textSize = chipTextSize
                        chip.text = it.mName
                        chip.doOnClickInterval { _ ->
                            dialog.cancel()
                            mSubtitleSuffix = it.mName
                            mToolbarSubtitle?.animateFadeIn()
                            mVM.setRegion(it.mPathWord)
                            updateComic()
                        }
                        binding.moreChipGroup.addView(chip)
                    }
                }
            }
            else -> error("Unknow menu type!")
        }
    }

    /** ● 初始化视图 */
    override fun initView(savedInstanceState: Bundle?) {

        // 设置Title
        mBinding.discoverComicAppbar.discoverAppbarToolbar.title = getString(R.string.discover_comic)

        // 初始化 发现页 漫画适配器
        mDiscoverComicAdapter = DiscoverComicAdapter { navigateBookComicInfo(it.mName, it.mPathWord) }

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
        mVM.onOutput { intent ->
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
                is DiscoverIntent.GetComicTag -> {
                    if (intent.showDialog) {
                        intent.mBaseViewState
                            .doOnError { _, _ -> toast(getString(baseR.string.BaseLoadingError)) }
                            .doOnResult { onSelectMenu(intent.type ?: return@doOnResult) }
                    }
                }
            }
        }
    }

    /** ● Lifecycle onDestroyView */
    override fun onDestroyView() {
        super.onDestroyView()
        AppGlideProgressFactory.doReset()
        parentFragmentManager.clearFragmentResultListener(COMIC)
        BaseEvent.getSIngleInstance().remove("DISCOVER_COMIC_FRAGMENT_POPULAR_ORDER")
        BaseEvent.getSIngleInstance().remove("DISCOVER_COMIC_FRAGMENT_UPDATE_ORDER")
        mToolbarSubtitle = null
    }
}