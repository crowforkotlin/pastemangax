package com.crow.module_home.ui.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import com.crow.base.copymanga.BaseStrings
import com.crow.base.copymanga.entity.Fragments
import com.crow.base.tools.coroutine.FlowBus
import com.crow.base.tools.coroutine.globalCoroutineException
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.tools.extensions.getStatusBarHeight
import com.crow.base.tools.extensions.navigateIconClickGap
import com.crow.base.tools.extensions.navigateToWithBackStack
import com.crow.base.tools.extensions.setCenterAnimWithFadeOut
import com.crow.base.tools.extensions.setFadeAnimation
import com.crow.base.tools.extensions.setMaskAmount
import com.crow.base.tools.extensions.showSnackBar
import com.crow.base.ui.dialog.LoadingAnimDialog
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.viewmodel.ViewState
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnLoading
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.base.ui.viewmodel.doOnSuccess
import com.crow.module_home.databinding.HomeFragmentBinding
import com.crow.module_home.model.intent.HomeIntent
import com.crow.module_home.model.resp.homepage.results.Results
import com.crow.module_home.ui.adapter.HomeComicParentRvAdapter
import com.crow.module_home.ui.viewmodel.HomeViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named
import com.crow.base.R as baseR

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_home/src/main/kotlin/com/crow/module_home/view
 * @Time: 2023/3/6 0:14
 * @Author: CrowForKotlin
 * @Description: HomeBodyFragment
 * @formatter:on
 **************************/

class HomeFragment : BaseMviFragment<HomeFragmentBinding>() {

    companion object { fun newInstance() = HomeFragment() }

    // 主页 VM
    private val mHomeVM by viewModel<HomeViewModel>()

    // 主页布局刷新的时间 第一次进入布局默认10Ms 之后刷新 为 50Ms
    private var mHomePageLayoutRefreshTime = 10L

    // 推荐 “换一批” 刷新按钮
    private var mRecRefresh: MaterialButton? = null

    // 主页数据量较多， 采用Rv方式
    private var mHomeComicParentRvAdapter: HomeComicParentRvAdapter? = null

    private val mRecRefreshCallback: (MaterialButton) -> Unit = {
        mRecRefresh = it
        mRecRefresh!!.isEnabled = false
        mHomeVM.input(HomeIntent.GetRecPageByRefresh())
    }


    // 注册FlowBus 设置主页头像
    init {
        FlowBus.with<Drawable>(BaseStrings.Key.SET_HOME_ICON).register(this) { drawable ->
            if (!isHidden) {
                lifecycleScope.launch(CoroutineName(this::class.java.simpleName) + globalCoroutineException) {
                    withStarted {
                        mBinding.homeToolbar.navigationIcon = drawable
                    }
                }
            }
        }
    }

    // 加载主页数据
    private fun doLoadHomePage(results: Results) {

        val datas = mutableListOf(
            results.mBanners.filter { banner -> banner.mType <= 2 }.toMutableList(),
            null, results.mRecComicsResult.mResult.toMutableList(), null,
            null, results.mHotComics.toMutableList(),
            null, results.mNewComics.toMutableList(),
            null, results.mFinishComicDatas.mResult.toMutableList(),
            null, results.mRankDayComics.mResult.toMutableList(),
            null, results.mTopics.mResult.toMutableList()
        )

        val isRefreshing = mBinding.homeRefresh.isRefreshing

        viewLifecycleOwner.lifecycleScope.launch {

            // 刷新控件动画消失
            if (isRefreshing) {
                mBinding.homeRefresh.finishRefresh()
                mHomeComicParentRvAdapter?.tryClearAndNotify()
                mHomeComicParentRvAdapter = null
                delay(200L)
                mHomeComicParentRvAdapter = HomeComicParentRvAdapter(datas.toMutableList(), viewLifecycleOwner, mRecRefreshCallback) { navigateBookComicInfo(it) }

                mBinding.homeRv.adapter = mHomeComicParentRvAdapter
            }
            
            else mHomeComicParentRvAdapter?.doNotify(datas.toMutableList(), false, 100L, 100L)

            // 取消加载动画
            dismissLoadingAnim()
        }
    }

    // 导航至BookInfo
    private fun navigateBookComicInfo(pathword: String) {
        val tag = Fragments.BookComicInfo.toString()
        val bundle = Bundle()
        bundle.putSerializable(BaseStrings.PATH_WORD, pathword)
        requireParentFragment().parentFragmentManager.navigateToWithBackStack(baseR.id.app_main_fcv,
            requireActivity().supportFragmentManager.findFragmentByTag(Fragments.Container.toString())!!,
            get<Fragment>(named(Fragments.BookComicInfo)).also { it.arguments = bundle }, tag, tag) { it.setCenterAnimWithFadeOut() }
    }

    // 导航至设置Fragment
    private fun navigateSettings() {
            requireParentFragment().parentFragmentManager.navigateToWithBackStack(baseR.id.app_main_fcv,
                requireActivity().supportFragmentManager.findFragmentByTag(Fragments.Container.toString())!!,
                get(named(Fragments.Settings)), Fragments.Settings.toString(), Fragments.Settings.toString()
            ) {
                it.setFadeAnimation()
            }
    }

    // 暴露的函数 提供给 ContainerFragment 用于通知主页刷新
    fun doRefresh() { mHomeVM.input(HomeIntent.GetHomePage()) }

    override fun getViewBinding(inflater: LayoutInflater) = HomeFragmentBinding.inflate(inflater)

    override fun onDestroyView() {
        super.onDestroyView()
        mRecRefresh = null // 置空“换一批”控件 防止内存泄漏
    }

    override fun initData() {

        // 获取主页数据
        mHomeVM.input(HomeIntent.GetHomePage())
    }

    override fun initView(bundle: Bundle?) {

        // 设置 内边距属性 实现沉浸式效果
        mBinding.homeAppbar.setPadding(0, mContext.getStatusBarHeight(), 0, 0)

        // 设置刷新时不允许列表滚动
        mBinding.homeRefresh.setDisableContentWhenRefresh(true)

        // 初始化适配器
        mHomeComicParentRvAdapter = HomeComicParentRvAdapter(mutableListOf(), viewLifecycleOwner, mRecRefreshCallback) { navigateBookComicInfo(it) }

        // 设置适配器
        mBinding.homeRv.adapter = mHomeComicParentRvAdapter
    }

    override fun initListener() {

        // 搜索
        mBinding.homeToolbar.menu[0].doOnClickInterval { mBinding.homeSearchView.show() }

        // 设置
        mBinding.homeToolbar.menu[1].doOnClickInterval { navigateSettings() }

        // MaterialToolBar NavigateIcon 点击事件
        mBinding.homeToolbar.navigateIconClickGap(true) { get<BottomSheetDialogFragment>(named(Fragments.User)).show(requireActivity().supportFragmentManager, null) }

        // 刷新
        mBinding.homeRefresh.setOnRefreshListener { mHomeVM.input(HomeIntent.GetHomePage()) }
    }

    override fun initObserver() {

        mHomeVM.onOutput { intent ->
            when (intent) {

                // （获取主页）（根据 刷新事件 来决定是否启用加载动画） 正常加载数据、反馈View
                is HomeIntent.GetHomePage -> {
                    intent.mViewState
                        .doOnLoading {
                            if(!mBinding.homeRefresh.isRefreshing && !isHidden) {
                                showLoadingAnim(object : LoadingAnimDialog.LoadingAnimConfig {
                                    override fun isNoInitStyle(): Boolean = true
                                    override fun doOnConfig(window: Window) {
                                        window.setMaskAmount(0.2f)
                                    }
                                })
                            }
                        }
                        .doOnResult {
                            // 刷新控件没有刷新 代表 用的是加载动画 -> 取消加载动画 否则直接加载页面数据
                            if (!mBinding.homeRefresh.isRefreshing) doLoadHomePage(intent.homePageData!!.mResults)
                            else doLoadHomePage(intent.homePageData!!.mResults)
                        }
                        .doOnError { code, msg ->
                            if (code == ViewState.Error.UNKNOW_HOST) mBinding.root.showSnackBar(msg ?: getString(baseR.string.BaseLoadingError))
                            if (!mBinding.homeRefresh.isRefreshing) dismissLoadingAnim() else mBinding.homeRefresh.finishRefresh()
                        }
                }

                // （刷新获取）不启用 加载动画 正常加载数据 -> 反馈View
                is HomeIntent.GetRecPageByRefresh -> {
                    intent.mViewState
                        .doOnSuccess { mRecRefresh?.isEnabled = true }
                        .doOnError { _, _ -> mBinding.root.showSnackBar(getString(baseR.string.BaseLoadingError)) }
                        .doOnResult {
                            viewLifecycleOwner.lifecycleScope.launch {
                                mHomeComicParentRvAdapter?.doRecNotify(intent.recPageData?.mResults?.mResult?.toMutableList() ?: return@launch)
                            }
                        }
                }
            }
        }
    }
}