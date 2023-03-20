package com.crow.module_home.ui.fragment

import android.graphics.Color
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.WRAP_CONTENT
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.crow.base.R.dimen
import com.crow.base.R.string.BaseLoadingError
import com.crow.base.extensions.*
import com.crow.base.tools.extensions.*
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.viewmodel.*
import com.crow.base.viewmodel.*
import com.crow.module_home.R
import com.crow.module_home.databinding.HomeComicBinding
import com.crow.module_home.databinding.HomeFragmentBinding
import com.crow.module_home.model.ComicType
import com.crow.module_home.model.intent.HomeIntent
import com.crow.module_home.model.resp.homepage.*
import com.crow.module_home.model.resp.homepage.results.RecComicsResult
import com.crow.module_home.model.resp.homepage.results.Results
import com.crow.module_home.ui.adapter.HomeBannerAdapter
import com.crow.module_home.ui.adapter.HomeComicAdapter
import com.crow.module_home.ui.viewmodel.HomeViewModel
import com.google.android.material.R.attr.materialIconButtonStyle
import com.google.android.material.button.MaterialButton
import com.to.aboomy.pager2banner.IndicatorView
import com.to.aboomy.pager2banner.ScaleInTransformer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_home/src/main/kotlin/com/crow/module_home/view
 * @Time: 2023/3/6 0:14
 * @Author: CrowForKotlin
 * @Description: HomeBodyFragment
 * @formatter:on
 **************************/
class HomeFragment constructor() : BaseMviFragment<HomeFragmentBinding>() {

    constructor(mClickListener: TapComicListener) : this() { mTapComicParentListener = mClickListener }

    interface TapComicListener { fun onTap(type: ComicType, pathword: String) }

    private val mHomeVM by viewModel<HomeViewModel>()

    // 主页 子Rv的漫画点击事件
    private var mTapComicChildListener = object : TapComicListener { override fun onTap(type: ComicType, pathword: String) { mTapComicParentListener?.onTap(type, pathword) } }

    // 主页 漫画点击事件
    private var mTapComicParentListener: TapComicListener? = null

    // 主页数据量较多， 采用Rv方式
    private lateinit var mHomeBannerAdapter: HomeBannerAdapter
    private lateinit var mHomeRecAdapter: HomeComicAdapter<ComicDatas<RecComicsResult>>
    private lateinit var mHomeHotAdapter: HomeComicAdapter<List<HotComic>>
    private lateinit var mHomeNewAdapter: HomeComicAdapter<List<NewComic>>
    private lateinit var mHomeFinishAdapter: HomeComicAdapter<FinishComicDatas>
    private lateinit var mHomeTopicAapter: HomeComicAdapter<ComicDatas<Topices>>
    private lateinit var mHomeRankAapter: HomeComicAdapter<ComicDatas<RankComics>>

    // 刷新按钮（换一批） ＆ 主页刷新 控件
    private var mRecRefreshButton : MaterialButton? = null
    private var mSwipeRefreshLayout : SwipeRefreshLayout? = null

    override fun getViewBinding(inflater: LayoutInflater) = HomeFragmentBinding.inflate(inflater)

    override fun onDestroyView() {
        super.onDestroyView()

        // 置空两个控件 避免内存泄漏
        mSwipeRefreshLayout = null
        mRecRefreshButton = null
    }

    override fun initObserver() {
        mHomeVM.onOutput { intent ->
            when (intent) {
                // （获取主页）（根据 刷新事件 来决定是否启用加载动画） 正常加载数据、反馈View
                is HomeIntent.GetHomePage -> {
                    intent.mViewState
                        .doOnLoading { if(mSwipeRefreshLayout == null) showLoadingAnim() }
                        .doOnResult {
                            // 检测是否为刷新
                            if (mSwipeRefreshLayout == null) dismissLoadingAnim { doOnLoadHomePage(intent.homePageData!!.mResults) }
                            else doOnLoadHomePage(intent.homePageData!!.mResults)
                        }
                        .doOnError { code, msg ->
                            if (code == ViewState.Error.UNKNOW_HOST) mBinding.root.showSnackBar(msg ?: getString(BaseLoadingError))
                            if (mSwipeRefreshLayout != null) mSwipeRefreshLayout?.isRefreshing = false
                            else dismissLoadingAnim()
                        }
                }

                // （刷新获取）不启用 加载动画 正常加载数据 反馈View
                is HomeIntent.GetRecPageByRefresh -> {
                    intent.mViewState
                        .doOnSuccess { mRecRefreshButton!!.isEnabled = true }
                        .doOnError { _, _ -> mBinding.root.showSnackBar(getString(BaseLoadingError)) }
                        .doOnResult {
                            mHomeRecAdapter.setData(intent.recPageData!!.mResults, 3)
                            viewLifecycleOwner.lifecycleScope.launch { mHomeRecAdapter.doOnNotify() }
                        }
                }
            }
        }
    }

    override fun initData() {

        // 重建View的同时 判断是否已获取数据
        if (mHomeVM.getResult() != null) return

        // 获取主页数据
        mHomeVM.input(HomeIntent.GetHomePage())
    }

    override fun initView() {

        // 适配器可以作为局部成员，但不要直接初始化，不然会导致被View引用从而内存泄漏
        mHomeBannerAdapter = HomeBannerAdapter(mutableListOf(), mTapComicChildListener)
        mHomeRecAdapter = HomeComicAdapter(null, ComicType.Rec, mTapComicChildListener)
        mHomeHotAdapter = HomeComicAdapter(null, ComicType.Hot, mTapComicChildListener)
        mHomeNewAdapter = HomeComicAdapter(null, ComicType.New, mTapComicChildListener)
        mHomeFinishAdapter = HomeComicAdapter(null, ComicType.Commit, mTapComicChildListener)
        mHomeTopicAapter = HomeComicAdapter(null, ComicType.Topic, mTapComicChildListener)
        mHomeRankAapter = HomeComicAdapter(null, ComicType.Rank, mTapComicChildListener)

        // 初始化刷新 推荐的按钮
        mRecRefreshButton = initRecRefreshView(mBinding.homeComicRec.homeComicButtonTitle.id)

        // 设置 Banner 的高度 （1.875 屏幕宽高指定倍数）、（添加页面效果、指示器、指示器需要设置BottomMargin不然会卡在Banner边缘（产生重叠））
        mBinding.homeBanner.doOnLayout { it.layoutParams.height = (it.width / 1.875 + 0.5).toInt() }
        mBinding.homeBanner.addPageTransformer(ScaleInTransformer())
            .setPageMargin(mContext.dp2px(20f), mContext.dp2px(10f))
            .setIndicator(
                IndicatorView(mContext)
                    .setIndicatorColor(Color.DKGRAY)
                    .setIndicatorSelectorColor(Color.WHITE)
                    .setIndicatorStyle(IndicatorView.IndicatorStyle.INDICATOR_BEZIER)
                    .also { it.doOnLayout { view -> (view.layoutParams as RelativeLayout.LayoutParams).bottomMargin = mContext.resources.getDimensionPixelSize(dimen.base_dp20) } })
            .adapter = mHomeBannerAdapter


        // 设置每一个子布局的 （Icon、标题、适配器）
        mBinding.homeComicRec.initHomeComicRvView(R.drawable.home_ic_recommed_24dp, R.string.home_recommend_comic, mHomeRecAdapter).also{ it.homeComicConstraint.addView(mRecRefreshButton) }
        mBinding.homeComicHot.initHomeComicRvView(R.drawable.home_ic_hot_24dp, R.string.home_hot_comic, mHomeHotAdapter)
        mBinding.homeComicNew.initHomeComicRvView(R.drawable.home_ic_new_24dp, R.string.home_new_comic, mHomeNewAdapter)
        mBinding.homeComicFinish.initHomeComicRvView(R.drawable.home_ic_finish_24dp, R.string.home_commit_finish, mHomeFinishAdapter)
        mBinding.homeComicTopic.initHomeComicRvView(R.drawable.home_ic_topic_24dp, R.string.home_topic_comic, mHomeTopicAapter).also { it.homeComicBookRv.layoutManager = GridLayoutManager(mContext, 2) }
        mBinding.homeComicRank.initHomeComicRvView(R.drawable.home_ic_rank_24dp, R.string.home_rank_comic, mHomeRankAapter)

        // 判断数据是否为空 不为空则加载数据
        doOnLoadHomePage(mHomeVM.getResult() ?: return)
    }

    override fun initListener() {
        mRecRefreshButton!!.setOnClickListener { view ->
            view.isEnabled = false
            mHomeVM.input(HomeIntent.GetRecPageByRefresh())
        }
        mBinding.homeComicRec.homeComicMore.clickGap { _, _ -> }
        mBinding.homeComicHot.homeComicMore.clickGap { _, _ -> }
        mBinding.homeComicNew.homeComicMore.clickGap { _, _ -> }
        mBinding.homeComicFinish.homeComicMore.clickGap { _, _ -> }
        mBinding.homeComicTopic.homeComicMore.clickGap { _, _ -> }
        mBinding.homeComicRank.homeComicMore.clickGap { _, _ -> }
    }

    // 初始化主页Rv视图
    private fun <T> HomeComicBinding.initHomeComicRvView(@DrawableRes iconRes: Int, @StringRes iconText: Int, adapter: HomeComicAdapter<T>): HomeComicBinding {
        homeComicButtonTitle.setIconResource(iconRes)
        homeComicButtonTitle.text = mContext.getString(iconText)
        homeComicBookRv.adapter = adapter
        return this
    }

    // 初始化刷新按钮
    private fun initRecRefreshView(@IdRes recItemBtId: Int): MaterialButton {
        return MaterialButton(mContext, null, materialIconButtonStyle).apply {
            layoutParams =  ConstraintLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                endToEnd = PARENT_ID
                topToBottom = mBinding.homeComicRec.homeComicBookRv.id
            }
            icon = ContextCompat.getDrawable(mContext, R.drawable.home_ic_refresh_24dp)
            iconSize = mContext.resources.getDimensionPixelSize(dimen.base_dp24)
            iconTint = null
            iconPadding = mContext.resources.getDimensionPixelSize(dimen.base_dp6)
            text = mContext.getString(R.string.home_refresh)
        }
    }

    // 加载主页数据
    private fun doOnLoadHomePage(results: Results) {

        // 设置适配器数据
        mHomeBannerAdapter.bannerList.clear()
        mHomeBannerAdapter.bannerList.addAll(results.mBanners.filter { banner -> banner.mType <= 2 })
        mHomeRecAdapter.setData(results.mRecComicsResult, 3)
        mHomeHotAdapter.setData(results.mHotComics, 12)
        mHomeNewAdapter.setData(results.mNewComics, 12)
        mHomeFinishAdapter.setData(results.mFinishComicDatas, 6)
        mHomeTopicAapter.setData(results.mTopics, 4)
        mHomeRankAapter.setData(results.mRankDayComics, 6)

        // 布局不可见 则淡入 否则代表正在刷新 提示即可
        if (!mBinding.homeLinearLayout.isVisible) {  mBinding.homeLinearLayout.animateFadeIn() }
        else toast("刷新成功~")

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {

            // 等待刷新控件动画消失 150MS
            if (mSwipeRefreshLayout != null && mSwipeRefreshLayout!!.isRefreshing) {
                mSwipeRefreshLayout!!.isRefreshing = false
                delay(150L)
            }

            // 通知适配器
            mHomeBannerAdapter.doOnNotify(waitTime = 0L)
            mHomeRecAdapter.doOnNotify(waitTime = 0L)
            mHomeHotAdapter.doOnNotify(waitTime = 0L)
            mHomeNewAdapter.doOnNotify(waitTime = 0L)
            mHomeFinishAdapter.doOnNotify(waitTime = 0L)
            mHomeTopicAapter.doOnNotify(waitTime = 0L)
            mHomeRankAapter.doOnNotify(waitTime = 0L)
        }
    }

    // 暴露的函数 提供给 ContainerFragment用于通知主页刷新
    fun doOnRefresh(swipeRefreshLayout: SwipeRefreshLayout) {
        mSwipeRefreshLayout = swipeRefreshLayout
        doAfterDelay(300L) { mHomeVM.input(HomeIntent.GetHomePage()) }
    }

}