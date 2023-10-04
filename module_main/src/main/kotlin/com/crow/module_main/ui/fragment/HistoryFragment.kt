package com.crow.module_main.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.addCallback
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.crow.base.R
import com.crow.base.copymanga.BaseLoadStateAdapter
import com.crow.base.copymanga.BaseStrings
import com.crow.base.copymanga.entity.Fragments
import com.crow.base.copymanga.processTokenError
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.animateFadeOutWithEndInVisibility
import com.crow.base.tools.extensions.navigateIconClickGap
import com.crow.base.tools.extensions.navigateToWithBackStack
import com.crow.base.tools.extensions.popSyncWithClear
import com.crow.base.tools.extensions.repeatOnLifecycle
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnSuccess
import com.crow.module_main.databinding.MainFragmentHistoryBinding
import com.crow.module_main.model.intent.MainIntent
import com.crow.module_main.ui.adapter.ComicHistoryListAdapter
import com.crow.module_main.ui.adapter.NovelHistoryListAdapter
import com.crow.module_main.ui.viewmodel.HistoryViewModel
import com.google.android.material.tabs.TabLayout
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
        ComicHistoryListAdapter { name, pathword ->
            navigate(Fragments.BookComicInfo.name, name, pathword)
        }
    }

    /**
     * ● 小说适配器
     *
     * ● 2023-10-04 17:00:17 周三 下午
     */
    private val mNovelAdapter by lazy {
        NovelHistoryListAdapter { name, pathword ->
            navigate(Fragments.BookNovelInfo.name, name, pathword)
        }
    }

    /**
     * ● 导航
     *
     * ● 2023-10-04 17:00:33 周三 下午
     */
    private fun navigate(tag: String, name: String, pathword: String) {
        val bundle = Bundle()
        bundle.putString(BaseStrings.PATH_WORD, pathword)
        bundle.putString(BaseStrings.NAME, name)
        parentFragmentManager.navigateToWithBackStack(
            id = R.id.app_main_fcv,
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
    }

    /**
     * ● 初始化观察者
     *
     * ● 2023-10-04 17:01:33 周三 下午
     */
    override fun initObserver(saveInstanceState: Bundle?) {

        repeatOnLifecycle {
            mVM.mComicHistoryFlowPager?.collect {
                mComicAdapter.submitData(it)
            }
        }

        repeatOnLifecycle {
            mVM.mNovelHistoryFlowPager?.collect {
                mNovelAdapter.submitData(it)
            }
        }

        mVM.onOutput { intent ->
            when(intent) {
                is MainIntent.GetComicHistory -> {
                    intent.mBaseViewState
                        .doOnError { code, msg ->
                            mBinding.root.processTokenError(code, msg) {
                                val tag = Fragments.Login.name
                                parentFragmentManager.navigateToWithBackStack(
                                    id = R.id.app_main_fcv,
                                    hideTarget = this,
                                    addedTarget = get(named(tag)),
                                    tag = tag,
                                    backStackName = tag
                                )
                            }
                        }
                        .doOnSuccess(mBinding.refresh::finishRefresh)
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
        mVM.input(MainIntent.GetComicHistory())
        mVM.input(MainIntent.GetNovelHistory())
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