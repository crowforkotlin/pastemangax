package com.crow.module_home.ui.fragment

import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Window
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.WRAP_CONTENT
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.crow.base.current_project.BaseStrings
import com.crow.base.current_project.entity.BookType
import com.crow.base.tools.coroutine.FlowBus
import com.crow.base.tools.extensions.*
import com.crow.base.ui.dialog.LoadingAnimDialog
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.viewmodel.*
import com.crow.module_home.R
import com.crow.module_home.databinding.HomeFragment2Binding
import com.crow.module_home.model.intent.HomeIntent
import com.crow.module_home.model.resp.homepage.results.RecComicsResult
import com.crow.module_home.model.resp.homepage.results.Results
import com.crow.module_home.ui.adapter.HomeBannerRvAdapter
import com.crow.module_home.ui.adapter.HomeComicRvAdapter2
import com.crow.module_home.ui.adapter.HomeComicRvAdapter3
import com.crow.module_home.ui.adapter.HomeComicRvAdapter4
import com.crow.module_home.ui.viewmodel.HomeViewModel
import com.google.android.material.R.attr.materialIconButtonStyle
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

    // 主页数据量较多， 采用Rv方式
    private lateinit var mHomeComicRvAdapter: HomeComicRvAdapter2

    // 初始化刷新按钮
    private fun initRecRefreshView(): MaterialButton {
        return MaterialButton(mContext, null, materialIconButtonStyle).apply {
            layoutParams = LinearLayoutCompat.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).also { it.gravity = Gravity.END or Gravity.CENTER_VERTICAL }
            icon = ContextCompat.getDrawable(mContext, R.drawable.home_ic_refresh_24dp)
            iconSize = mContext.resources.getDimensionPixelSize(baseR.dimen.base_dp24)
            iconTint = null
            iconPadding = mContext.resources.getDimensionPixelSize(baseR.dimen.base_dp6)
            text = mContext.getString(R.string.home_refresh)
        }
    }

    // 加载主页数据
    private fun doLoadHomePage(results: Results) {

        // 刷新控件动画消失
        if (mBinding.homeRefresh.isRefreshing) mBinding.homeRefresh.finishRefresh()

        /*mHomeComicRvAdapter = HomeComicRvAdapter2((mutableListOf(
            results.mBanners.filter { banner -> banner.mType <= 2 }.toMutableList(),
            results.mRecComicsResult.mResult.toMutableList(),
            results.mHotComics.toMutableList(),
            results.mNewComics.toMutableList(),
            results.mFinishComicDatas.mResult.toMutableList(),
            results.mRankDayComics.mResult.toMutableList(),
            results.mTopics.mResult.toMutableList()
        )), viewLifecycleOwner) { _, _ -> }
*/
        mHomeComicRvAdapter = HomeComicRvAdapter2((mutableListOf(
            HomeBannerRvAdapter(results.mBanners.filter { banner -> banner.mType <= 2 }.toMutableList()) { _, _ -> },
            HomeComicRvAdapter3(results.mRecComicsResult.mResult.toMutableList() ,mBookType = BookType.Rec) { _, _ -> },
            HomeComicRvAdapter3(results.mHotComics.toMutableList() ,mBookType = BookType.Hot) { _, _ -> },
            HomeComicRvAdapter3(results.mNewComics.toMutableList() ,mBookType = BookType.New) { _, _ -> },
            HomeComicRvAdapter3(results.mFinishComicDatas.mResult.toMutableList() ,mBookType = BookType.Finish) { _, _ -> },
            HomeComicRvAdapter3(results.mRankDayComics.mResult.toMutableList() ,mBookType = BookType.Rank) { _, _ -> },
            HomeComicRvAdapter3(results.mTopics.mResult.toMutableList() ,mBookType = BookType.Topic) { _, _ -> },
        )), viewLifecycleOwner) { _, _ -> }


        mBinding.homeRv.adapter = mHomeComicRvAdapter

        // 取消加载动画
        dismissLoadingAnim()
    }

    // 暴露的函数 提供给 ContainerFragment 用于通知主页刷新
    fun doRefresh() {
        mHomeVM.input(HomeIntent.GetHomePage())
    }

    // 暴露的函数 提供给 ContainerFragment 用于通知主页设置Icon
    fun setIconResource(resource: Drawable) {
        repeatOnLifecycle(Lifecycle.State.RESUMED) {
            mBinding.homeToolbar.navigationIcon = resource
        }
    }

    override fun getViewBinding(inflater: LayoutInflater) = HomeFragment2Binding.inflate(inflater)

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
                        .doOnSuccess { }
                        .doOnError { _, _ -> mBinding.root.showSnackBar(getString(baseR.string.BaseLoadingError)) }
                        .doOnResult {
                            viewLifecycleOwner.lifecycleScope.launch {
                            }
                        }
                }
            }
        }
    }
}