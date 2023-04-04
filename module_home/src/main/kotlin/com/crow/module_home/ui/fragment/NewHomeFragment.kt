package com.crow.module_home.ui.fragment

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.Window
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import com.crow.base.current_project.BaseStrings
import com.crow.base.tools.coroutine.FlowBus
import com.crow.base.tools.extensions.*
import com.crow.base.ui.dialog.LoadingAnimDialog
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.viewmodel.*
import com.crow.module_home.databinding.HomeFragment2Binding
import com.crow.module_home.model.intent.HomeIntent
import com.crow.module_home.model.resp.homepage.results.Results
import com.crow.module_home.ui.adapter.HomeComicRvAdapter2
import com.crow.module_home.ui.viewmodel.HomeViewModel
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.crow.base.R as baseR

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_home/src/main/kotlin/com/crow/module_home/view
 * @Time: 2023/3/6 0:14
 * @Author: CrowForKotlin
 * @Description: HomeBodyFragment
 * @formatter:on
 **************************/

class NewHomeFragment : BaseMviFragment<HomeFragment2Binding>() {

    companion object { fun newInstance() = NewHomeFragment() }

    // 主页 VM
    private val mHomeVM by viewModel<HomeViewModel>()

    // 主页布局刷新的时间 第一次进入布局默认10Ms 之后刷新 为 50Ms
    private var mHomePageLayoutRefreshTime = 10L

    // 推荐 “换一批” 刷新按钮
    private var mRecRefresh: MaterialButton? = null

    // 主页数据量较多， 采用Rv方式
    private var mHomeComicRvAdapter: HomeComicRvAdapter2? = null

    init { FlowBus.with<Drawable>(BaseStrings.Key.SET_HOME_ICON).register(this) { mBinding.homeToolbar.navigationIcon = it } }

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


        viewLifecycleOwner.lifecycleScope.launch {

            // 刷新控件动画消失
            if (mBinding.homeRefresh.isRefreshing) {
                mBinding.homeRefresh.finishRefresh()
                mHomeComicRvAdapter?.doNotify(datas.toMutableList(), true,100L, 0L)
            }

            else mHomeComicRvAdapter?.doNotify(datas.toMutableList(), false, 100L, 100L)


            // 取消加载动画
            dismissLoadingAnim()
        }
    }

    // 暴露的函数 提供给 ContainerFragment 用于通知主页刷新
    fun doRefresh() { mHomeVM.input(HomeIntent.GetHomePage()) }

    override fun getViewBinding(inflater: LayoutInflater) = HomeFragment2Binding.inflate(inflater)

    override fun onDestroyView() {
        super.onDestroyView()
        mRecRefresh = null
    }

    override fun initData() {

        // 重建View的同时 判断是否已获取数据
        if (mHomeVM.getResult() != null) return

        // 获取主页数据
        mHomeVM.input(HomeIntent.GetHomePage())
    }

    override fun initView() {

        // 设置 内边距属性 实现沉浸式效果
        mBinding.homeAppbar.setPadding(0, mContext.getStatusBarHeight(), 0, 0)

        // 设置刷新时不允许列表滚动
        mBinding.homeRefresh.setDisableContentWhenRefresh(true)

        mHomeComicRvAdapter = HomeComicRvAdapter2(mutableListOf(), viewLifecycleOwner = viewLifecycleOwner) {
            mRecRefresh = it
            mRecRefresh!!.isEnabled = false
            mHomeVM.input(HomeIntent.GetRecPageByRefresh())
        }

        // 设置适配器
        mBinding.homeRv.adapter = mHomeComicRvAdapter

        doLoadHomePage(mHomeVM.getResult() ?: return)
    }

    override fun initListener() {

        // 搜索
        mBinding.homeToolbar.menu[0].clickGap { _, _ ->
            mBinding.homeSearchView.show()
        }

        // 设置
        mBinding.homeToolbar.menu[1].clickGap { _, _ ->
            mContext.newMaterialDialog { dialog ->
                dialog.setTitle("拷贝漫画")
                dialog.setPositiveButton("知道了~", null)
            }
        }

        // MaterialToolBar NavigateIcon 点击事件
        mBinding.homeToolbar.navigateIconClickGap { _, _ ->
            FlowBus.with<Unit>(BaseStrings.Key.OPEN_USER_BOTTOM).post(lifecycleScope, Unit)
        }

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
                            if(!mBinding.homeRefresh.isRefreshing) {
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
                                mHomeComicRvAdapter?.doRecNotify(intent.recPageData?.mResults?.mResult?.toMutableList() ?: return@launch)
                            }
                        }
                }
            }
        }
    }
}