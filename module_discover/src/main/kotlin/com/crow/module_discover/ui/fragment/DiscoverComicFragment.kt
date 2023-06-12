package com.crow.module_discover.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.view.get
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import androidx.lifecycle.withStarted
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import com.crow.base.copymanga.BaseEventEnum
import com.crow.base.copymanga.BaseLoadStateAdapter
import com.crow.base.copymanga.BaseStrings
import com.crow.base.copymanga.entity.Fragments
import com.crow.base.copymanga.glide.AppGlideProgressFactory
import com.crow.base.tools.coroutine.FlowBus
import com.crow.base.tools.coroutine.globalCoroutineException
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.animateFadeOutWithEndInVisibility
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.tools.extensions.immersionPadding
import com.crow.base.tools.extensions.navigateToWithBackStack
import com.crow.base.tools.extensions.repeatOnLifecycle
import com.crow.base.tools.extensions.showSnackBar
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.viewmodel.BaseViewState
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.base.ui.viewmodel.doOnSuccess
import com.crow.module_discover.R
import com.crow.module_discover.databinding.DiscoverFragmentComicBinding
import com.crow.module_discover.model.intent.DiscoverIntent
import com.crow.module_discover.ui.adapter.DiscoverComicAdapter
import com.crow.module_discover.ui.viewmodel.DiscoverViewModel
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.launch
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

    init {
        FlowBus.with<Int>(BaseEventEnum.SelectPage.name).register(this) {
            if (it == 1 && !isHidden) {
                lifecycleScope.launch(CoroutineName(this::class.java.simpleName) + globalCoroutineException) {
                    withStarted {
                        repeatOnLifecycle { mDiscoverVM.mDiscoverComicHomeFlowPager?.collect { mDiscoverComicAdapter.submitData(it) } }
                    }
                }
            }
        }
    }

    companion object { fun newInstance() = DiscoverComicFragment() }

    /** ● (Activity级别) 发现VM */
    private val mDiscoverVM by sharedViewModel<DiscoverViewModel>()

    /** ● 漫画适配器 */
    private lateinit var mDiscoverComicAdapter: DiscoverComicAdapter

    /** ● 获取ViewBinding */
    override fun getViewBinding(inflater: LayoutInflater) = DiscoverFragmentComicBinding.inflate(inflater)

    /** ● 初始化监听事件 */
    override fun initListener() {

        // 刷新监听
        mBinding.discoverComicRefresh.setOnRefreshListener { mDiscoverComicAdapter.refresh() }

        mBinding.discoverComicAppbar.discoverAppbarToolbar.menu[0].doOnClickInterval {

        }

        // Rv滑动监听
        /*mBinding.discoverComicRv.setOnScrollChangeListener { _, _, _, _, _ ->
            val layoutManager = mBinding.discoverComicRv.layoutManager
            if(layoutManager is LinearLayoutManager) mBinding.discoverComicAppbar.discoverAppbarTextPos.text = (layoutManager.findLastVisibleItemPosition().plus(1)).toString()
        }*/
    }

    /** ● 导航至漫画页 */
    private fun navigateBookComicInfo(pathword: String) {
        val bundle = Bundle()
        bundle.putSerializable(BaseStrings.PATH_WORD, pathword)
        requireParentFragment().parentFragmentManager.navigateToWithBackStack(baseR.id.app_main_fcv,
            requireActivity().supportFragmentManager.findFragmentByTag(Fragments.Container.name)!!,
            get<Fragment>(named(Fragments.BookComicInfo.name)).also { it.arguments = bundle }, Fragments.BookComicInfo.name, Fragments.BookComicInfo.name
        )
    }

    /** ● 收集状态 */
    fun onCollectState() {
        lifecycleScope.launch {
            whenCreated {
                // 收集状态 通知适配器
                repeatOnLifecycle { mDiscoverVM.mDiscoverComicHomeFlowPager?.collect { mDiscoverComicAdapter.submitData(it) } }
            }
        }
    }

    /** ● 初始化视图 */
    override fun initView(savedInstanceState: Bundle?) {

        // 设置 内边距属性 实现沉浸式效果
        mBinding.discoverComicAppbar.root.immersionPadding(hideNaviateBar = false)

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
    override fun initObserver() {

        // 意图观察者
        mDiscoverVM.onOutput { intent ->
            when(intent) {
                is DiscoverIntent.GetComicHome -> {
                    intent.mBaseViewState
                        .doOnSuccess { if (mBinding.discoverComicRefresh.isRefreshing) mBinding.discoverComicRefresh.finishRefresh() }
                        .doOnError { code, msg ->

                            // 解析地址失败 且 选中的时当前页面的状态才提示
                            if (code == BaseViewState.Error.UNKNOW_HOST && mDiscoverVM.mCurrentItem == 1) mBinding.root.showSnackBar(msg ?: getString(com.crow.base.R.string.BaseLoadingError))

                            if (mDiscoverComicAdapter.itemCount == 0) {
                                if (mDiscoverVM.mCurrentItem == 1) {

                                    // 错误提示淡入
                                    mBinding.discoverComicTipsError.animateFadeIn()

                                    // “标签”文本 淡出
                                    // mBinding.discoverComicAppbar.discoverAppbarTagText.animateFadeOutWithEndInVisibility()

                                    // 发现页 “漫画” 淡出
                                    mBinding.discoverComicRv.animateFadeOutWithEndInVisibility()

                                    // “当前位置”文本 淡出
                                    // mBinding.discoverComicAppbar.discoverAppbarTextPos.animateFadeOut().withEndAction { mBinding.discoverComicRv.isInvisible = true }
                                }

                                // 这里没有使用淡入淡出动画 而是直接设置可见性，因为在未预览当前页面的时候（在主界面没必要使用动画，减少卡顿）
                                else {
                                    mBinding.discoverComicTipsError.isVisible = true                        // 错误提示可见
                                    mBinding.discoverComicRv.isInvisible = true                             // 漫画Rv不可见
                                    // mBinding.discoverComicAppbar.discoverAppbarTagText.isInvisible = true   // 标签文本消失
                                    // mBinding.discoverComicAppbar.discoverAppbarTextPos.isInvisible = true   // 当前位置消失
                                }
                            }
                        }
                        .doOnResult {
                            // 错误提示 可见
                            if (mBinding.discoverComicTipsError.isVisible) {
                                // mBinding.discoverComicAppbar.discoverAppbarTagText.text = "全部 — 全部"
                                // mBinding.discoverComicAppbar.discoverAppbarPosTotal.text = intent.comicHomeResp!!.mTotal.toString()

                                // 若 VP 显示的是当前页 则动画淡入 否则直接显示（减少动画带来的卡顿）
                                if (mDiscoverVM.mCurrentItem == 1) {
                                    mBinding.discoverComicTipsError.animateFadeOutWithEndInVisibility()
                                    // mBinding.discoverComicAppbar.discoverAppbarTagText.animateFadeIn()
                                    // mBinding.discoverComicAppbar.discoverAppbarTextPos.animateFadeIn()
                                    mBinding.discoverComicRv.animateFadeIn()
                                } else {
                                    mBinding.discoverComicTipsError.isVisible = false
                                    // mBinding.discoverComicAppbar.discoverAppbarTagText.isVisible = true
                                    // mBinding.discoverComicAppbar.discoverAppbarTextPos.isVisible = true
                                    mBinding.discoverComicRv.isVisible = true
                                }
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


    /** ● Lifecycle onDestroyView */
    override fun onDestroyView() {
        super.onDestroyView()
        AppGlideProgressFactory.doReset()
    }
}