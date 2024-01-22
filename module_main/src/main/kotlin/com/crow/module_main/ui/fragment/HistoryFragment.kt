package com.crow.module_main.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.addCallback
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.crow.base.R
import com.crow.mangax.R as mangaR
import com.crow.mangax.copymanga.BaseLoadStateAdapter
import com.crow.mangax.copymanga.BaseStrings
import com.crow.mangax.copymanga.entity.Fragments
import com.crow.mangax.copymanga.processTokenError
import com.crow.base.kt.BaseNotNullVar
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.animateFadeOutWithEndInVisibility
import com.crow.base.tools.extensions.navigateIconClickGap
import com.crow.base.tools.extensions.navigateToWithBackStack
import com.crow.base.tools.extensions.onCollect
import com.crow.base.tools.extensions.popSyncWithClear
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.view.BaseErrorViewStub
import com.crow.base.ui.view.baseErrorViewStub
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.base.ui.viewmodel.doOnSuccess
import com.crow.module_main.databinding.MainFragmentHistoryBinding
import com.crow.module_main.model.intent.MainIntent
import com.crow.module_main.ui.adapter.ComicHistoryListAdapter
import com.crow.module_main.ui.adapter.NovelHistoryListAdapter
import com.crow.module_main.ui.viewmodel.HistoryViewModel
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Package: com.crow.module_main.ui.fragment
 * @Time: 2023/10/2 19:36
 * @Author: CrowForKotlin
 * @Description: HistoryFragment
 * @formatter:on
 **************************/
class HistoryFragment : BaseMviFragment<MainFragmentHistoryBinding>() {

    /**
     * ● History VM
     *
     * ● 2023-10-03 18:37:31 周二 下午
     */
    private val mVM by viewModel<HistoryViewModel>()

    /**
     * ● 漫画适配器
     *
     * ● 2023-10-04 16:59:58 周三 下午
     */
    private val mComicAdapter by lazy {
        ComicHistoryListAdapter(lifecycleScope) { name, pathword ->
            onNavigate(Fragments.BookComicInfo.name, name, pathword)
        }
    }

    /**
     * ● 小说适配器
     *
     * ● 2023-10-04 17:00:17 周三 下午
     */
    private val mNovelAdapter by lazy {
        NovelHistoryListAdapter(lifecycleScope) { name, pathword ->
            onNavigate(Fragments.BookNovelInfo.name, name, pathword)
        }
    }

    /**
     * ● 网络任务
     *
     * ● 2023-11-01 23:50:26 周三 下午
     * @author crowforkotlin
     */
    private var mNetworkJob: Job? = null

    /**
     * ● 错误视图
     *
     * ● 2023-11-02 00:01:07 周四 上午
     * @author crowforkotlin
     */
    private var mError by BaseNotNullVar<BaseErrorViewStub>(true)

    /**
     * ● 导航
     *
     * ● 2023-10-04 17:00:33 周三 下午
     */
    private fun onNavigate(tag: String, name: String, pathword: String) {
        val bundle = Bundle()
        bundle.putString(BaseStrings.PATH_WORD, pathword)
        bundle.putString(BaseStrings.NAME, name)
        parentFragmentManager.navigateToWithBackStack(
            id = mangaR.id.app_main_fcv,
            hideTarget = this,
            addedTarget = get<Fragment>(named(tag)).also { it.arguments = bundle },
            tag = tag,
            backStackName = tag
        )
    }

    /**
     * ● 获取VB
     *
     * ● 2023-10-04 17:00:39 周三 下午
     */
    override fun getViewBinding(inflater: LayoutInflater) = MainFragmentHistoryBinding.inflate(layoutInflater)

    /**
     * ● 初始化视图
     *
     * ● 2023-10-04 17:00:47 周三 下午
     */
    override fun initView(savedInstanceState: Bundle?) {

        // 设置 内边距属性 实现沉浸式效果
        immersionRoot()

        // 初始化错误视图
        mError = baseErrorViewStub(mBinding.error, lifecycle) { mBinding.refresh.autoRefresh() }

        // 设置刷新时不允许列表滚动
        mBinding.refresh.setDisableContentWhenRefresh(true)

        // 刷新
        mBinding.refresh.autoRefreshAnimationOnly()

        // 初始化Rv
        when(mBinding.tablayout.selectedTabPosition) {
            0 -> initComicList()
            1 -> initNovelList()
        }
    }

    /**
     * ● 初始化监听器
     *
     * ● 2023-10-04 17:00:54 周三 下午
     */
    override fun initListener() {

        // 返回
        mBinding.topbar.navigateIconClickGap { navigateUp() }

        // 刷新监听
        mBinding.refresh.setOnRefreshListener {
            mComicAdapter.retry()
            mNovelAdapter.retry()
            mBinding.refresh.finishRefresh()
        }

        // Tablayout 事件
        mBinding.tablayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(mBinding.tablayout.selectedTabPosition) {
                    0 -> {
                        if (mBinding.comicList.adapter == null) initComicList()
                        mBinding.novelList.animateFadeOutWithEndInVisibility()
                        mBinding.comicList.animateFadeIn()
                    }
                    1 -> {
                        if (mBinding.novelList.adapter == null) initNovelList()
                        mBinding.comicList.animateFadeOutWithEndInVisibility()
                        mBinding.novelList.animateFadeIn()
                    }
                }
            }
        })

        // 返回
        mBinding.topbar.navigateIconClickGap { navigateUp() }
    }

    /**
     * ● 初始化观察者
     *
     * ● 2023-10-04 17:01:33 周三 下午
     */
    override fun initObserver(saveInstanceState: Bundle?) {

        mVM.onOutput { intent ->
            when(intent) {
                is MainIntent.GetComicHistory -> {
                    intent.mViewState
                        .doOnSuccess(mBinding.refresh::finishRefresh)
                        .doOnError { code, msg ->
                            mBinding.root.processTokenError(code, msg) {
                                val tag = Fragments.Login.name
                                parentFragmentManager.navigateToWithBackStack(
                                    id = mangaR.id.app_main_fcv,
                                    hideTarget = this,
                                    addedTarget = get(named(tag)),
                                    tag = tag,
                                    backStackName = tag
                                )
                            }
                            if (mComicAdapter.itemCount == 0) {
                                mBinding.comicList.animateFadeOutWithEndInVisibility()
                                if (mError.isGone()) mError.loadLayout(visible = true, animation = true)
                            }
                            if (mNovelAdapter.itemCount == 0) {
                                mBinding.novelList.animateFadeOutWithEndInVisibility()
                                if (mError.isGone()) mError.loadLayout(visible = true, animation = true)
                            }
                        }
                        .doOnResult {
                            if (mError.isVisible()) { mError.loadLayout(visible = false, animation = true) }
                            when(mBinding.tablayout.selectedTabPosition) {
                                0  -> {
                                    if (mBinding.comicList.isInvisible) {
                                        mBinding.comicList.animateFadeIn()
                                    }
                                }
                                else -> {
                                    if (mBinding.novelList.isInvisible) {
                                        mBinding.novelList.animateFadeIn()
                                    }
                                }
                            }
                        }
                }
            }
        }
    }

    /**
     * ● Lifecycle onCreate
     *
     * ● 2023-10-04 17:01:06 周三 下午
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mNetworkJob = lifecycleScope.launch {
            delay(BASE_ANIM_200L shl 1)
            mVM.input(MainIntent.GetComicHistory()) { mVM.mComicHistoryFlowPager?.onCollect(this@HistoryFragment) { mComicAdapter.submitData(it) } }
            mVM.input(MainIntent.GetNovelHistory()) { mVM.mNovelHistoryFlowPager?.onCollect(this@HistoryFragment) { mNovelAdapter.submitData(it) } }
        }
    }

    /**
     * ● Lifecycle onDestroyView
     *
     * ● 2023-11-01 23:51:04 周三 下午
     * @author crowforkotlin
     */
    override fun onDestroyView() {
        super.onDestroyView()
        mNetworkJob?.cancel()
        mNetworkJob = null
    }

    /**
     * ● Lifecycle onStart
     *
     * ● 2023-10-04 17:01:41 周三 下午
     */
    override fun onStart() {
        super.onStart()
        mBackDispatcher = requireActivity().onBackPressedDispatcher.addCallback(this) { navigateUp() }
    }

    /**
     * ● 返回
     *
     * ● 2023-10-04 17:01:50 周三 下午
     */
    private fun navigateUp() = parentFragmentManager.popSyncWithClear(Fragments.History.name)

    /**
     * ● 初始化漫画列表
     *
     * ● 2023-10-04 17:01:58 周三 下午
     */
    private fun initComicList() {

        // 可见
        if (mBinding.comicList.isInvisible) mBinding.comicList.isVisible = true

        // 初始化适配器
        mBinding.comicList.adapter = mNovelAdapter

        // 添加Footer
        mBinding.comicList.adapter = mComicAdapter.withLoadStateFooter(BaseLoadStateAdapter { mComicAdapter.retry() })
    }

    /**
     * ● 初始化轻小说列表
     *
     * ● 2023-10-04 17:02:07 周三 下午
     */
    private fun initNovelList() {

        // 可见
        if (mBinding.novelList.isInvisible) mBinding.novelList.isVisible = true

        // 初始化适配器
        mBinding.novelList.adapter = mNovelAdapter

        // 添加Footer
        mBinding.novelList.adapter = mNovelAdapter.withLoadStateFooter(BaseLoadStateAdapter { mNovelAdapter.retry() })
    }
}