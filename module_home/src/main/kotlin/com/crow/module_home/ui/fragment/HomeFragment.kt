package com.crow.module_home.ui.fragment

import android.graphics.Color
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.WRAP_CONTENT
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.core.view.setMargins
import androidx.recyclerview.widget.GridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.crow.base.R.dimen
import com.crow.base.extensions.animateFadeIn
import com.crow.base.extensions.dp2px
import com.crow.base.extensions.showSnackBar
import com.crow.base.extensions.toast
import com.crow.base.fragment.BaseMviFragment
import com.crow.base.viewmodel.ViewState
import com.crow.base.viewmodel.doOnError
import com.crow.base.viewmodel.doOnLoading
import com.crow.base.viewmodel.doOnResult
import com.crow.module_home.R
import com.crow.module_home.databinding.HomeFragmentBinding
import com.crow.module_home.databinding.HomeRvItemLayoutBinding
import com.crow.module_home.model.ComicType
import com.crow.module_home.model.intent.HomeIntent
import com.crow.module_home.model.resp.homepage.*
import com.crow.module_home.model.resp.homepage.results.RecComicsResult
import com.crow.module_home.model.resp.homepage.results.Results
import com.crow.module_home.ui.adapter.HomeBannerAdapter
import com.crow.module_home.ui.adapter.HomeBookAdapter
import com.crow.module_home.ui.viewmodel.HomeViewModel
import com.google.android.material.R.attr.materialIconButtonStyle
import com.google.android.material.button.MaterialButton
import com.to.aboomy.pager2banner.IndicatorView
import com.to.aboomy.pager2banner.ScaleInTransformer
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

    constructor(clickListener: TapComicListener) : this() { mTapComicParentListener = clickListener }

    interface TapComicListener { fun onTap(type: ComicType, pathword: String) }

    private var mTapComicChildListener = object : TapComicListener { override fun onTap(type: ComicType, pathword: String) { mTapComicParentListener?.onTap(type, pathword) } }
    private var mTapComicParentListener: TapComicListener? = null
    private var mRefreshListener: OnRefreshListener? = null
    private val mHomeVM by viewModel<HomeViewModel>()

    // 主页数据量较多 后期看看可不可以改成双Rv实现 适配器太多了
    private lateinit var mHomeBannerAdapter: HomeBannerAdapter
    private lateinit var mHomeRecAdapter: HomeBookAdapter<ComicDatas<RecComicsResult>>
    private lateinit var mHomeHotAdapter: HomeBookAdapter<List<HotComic>>
    private lateinit var mHomeNewAdapter: HomeBookAdapter<List<NewComic>>
    private lateinit var mHomeCommitAdapter: HomeBookAdapter<FinishComicDatas>
    private lateinit var mHomeTopicAapter: HomeBookAdapter<ComicDatas<Topices>>
    private lateinit var mHomeRankAapter: HomeBookAdapter<ComicDatas<RankComics>>
    private lateinit var mRefreshButton : MaterialButton

    // 记录已经滑动的Y位置 用于View重建时定位
    private var mScrollY = 0

    override fun getViewBinding(inflater: LayoutInflater) = HomeFragmentBinding.inflate(inflater)

    override fun initObserver() {
        mHomeVM.onOutput { intent ->
            when (intent) {
                // （获取主页）（根据 刷新事件 来决定是否启用加载动画） 正常加载数据、反馈View
                is HomeIntent.GetHomePage -> {
                    intent.mViewState
                        .doOnLoading { if(mRefreshListener == null) showLoadingAnim() }
                        .doOnResult {
                            doOnLoadHomePage(intent.homePageData!!.mResults)
                            if(mRefreshListener != null) showHomePage() else dismissLoadingAnim { showHomePage() }
                        }
                        .doOnError { code, msg ->
                            if (code == ViewState.Error.UNKNOW_HOST) mBinding.root.showSnackBar(msg ?: "")
                            if (mRefreshListener != null) { mRefreshListener?.onRefresh() }
                            dismissLoadingAnim { mBinding.homeLinearLayout.animateFadeIn(300L) }
                        }
                }

                // （刷新获取）不启用 加载动画 正常加载数据 反馈View
                is HomeIntent.GetRecPageByRefresh -> {
                    intent.mViewState
                        .doOnError { _, _ -> mRefreshButton.isEnabled = true }
                        .doOnResult {
                            mHomeRecAdapter.setData(intent.recPageData!!.mResults)
                            mHomeRecAdapter.notifyItemRangeChanged(0, mHomeRecAdapter.getDataSize())
                            mRefreshButton.isEnabled = true
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
        mHomeRecAdapter = HomeBookAdapter(null, ComicType.Rec, mTapComicChildListener)
        mHomeHotAdapter = HomeBookAdapter(null, ComicType.Hot, mTapComicChildListener)
        mHomeNewAdapter = HomeBookAdapter(null, ComicType.New, mTapComicChildListener)
        mHomeCommitAdapter = HomeBookAdapter(null, ComicType.Commit, mTapComicChildListener)
        mHomeTopicAapter = HomeBookAdapter(null, ComicType.Topic, mTapComicChildListener)
        mHomeRankAapter = HomeBookAdapter(null, ComicType.Rank, mTapComicChildListener)

        // 初始化刷新 推荐的按钮
        mRefreshButton = initRefreshButton()

        // 设置 Banner 的高度 （1.875 屏幕宽高指定倍数）、（添加页面效果、指示器、指示器需要设置BottomMargin不然会卡在Banner边缘（产生重叠））
        mBinding.homeBanner.doOnLayout { it.layoutParams.height = (it.width / 1.875 + 0.5).toInt() }
        mBinding.homeBanner.addPageTransformer(ScaleInTransformer())
            .setPageMargin(mContext.dp2px(20), mContext.dp2px(10))
            .setIndicator(
                IndicatorView(mContext)
                    .setIndicatorColor(Color.DKGRAY)
                    .setIndicatorSelectorColor(Color.WHITE)
                    .setIndicatorStyle(IndicatorView.IndicatorStyle.INDICATOR_BEZIER)
                    .also { it.doOnLayout { view -> (view.layoutParams as RelativeLayout.LayoutParams).bottomMargin = mContext.resources.getDimensionPixelSize(dimen.base_dp20) } })
            .adapter = mHomeBannerAdapter


        // 设置每一个子布局的 （Icon、标题、适配器）
        mBinding.homeItemRec.initHomeItem(R.drawable.home_ic_recommed_24dp, R.string.home_recommend_comic, mHomeRecAdapter).also{ it.homeItemConstraint.addView(mRefreshButton) }
        mBinding.homeItemHot.initHomeItem(R.drawable.home_ic_hot_24dp, R.string.home_hot_comic, mHomeHotAdapter)
        mBinding.homeItemNew.initHomeItem(R.drawable.home_ic_new_24dp, R.string.home_new_comic, mHomeNewAdapter)
        mBinding.homeItemCommit.initHomeItem(R.drawable.home_ic_commit_24dp, R.string.home_commit_comic, mHomeCommitAdapter)
        mBinding.homeItemTopic.initHomeItem(R.drawable.home_ic_topic_24dp, R.string.home_topic_comic, mHomeTopicAapter).also { it.homeItemBookRv.layoutManager = GridLayoutManager(mContext, 2) }
        mBinding.homeItemRank.initHomeItem(R.drawable.home_ic_rank_24dp, R.string.home_rank_comic, mHomeRankAapter)

        // 判断数据是否为空 不为空则加载数据
        doOnLoadHomePage(mHomeVM.getResult() ?: return)
        showHomePage()
    }

    override fun initListener() {
        mRefreshButton.setOnClickListener {
            it.isEnabled = false
            mHomeVM.input(HomeIntent.GetRecPageByRefresh())
        }
    }

    override fun onPause() {
        super.onPause()
        mScrollY = mBinding.root.scrollY
        mRefreshListener = null
    }

    private fun <T> HomeRvItemLayoutBinding.initHomeItem(@DrawableRes iconRes: Int, @StringRes iconText: Int, adapter: HomeBookAdapter<T>): HomeRvItemLayoutBinding {
        homeItemBt.setIconResource(iconRes)
        homeItemBt.text = mContext.getString(iconText)
        homeItemBookRv.adapter = adapter
        return this
    }

    private fun initRefreshButton(): MaterialButton {
        val recItemBtId = mBinding.homeItemRec.homeItemBt.id
        val constraintParams = ConstraintLayout.LayoutParams(0, WRAP_CONTENT)
        constraintParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        constraintParams.topToTop = recItemBtId
        constraintParams.bottomToBottom = recItemBtId
        constraintParams.setMargins(mContext.resources.getDimensionPixelSize(dimen.base_dp5))
        return MaterialButton(mContext, null, materialIconButtonStyle).apply {
            layoutParams = constraintParams
            icon = ContextCompat.getDrawable(mContext, R.drawable.home_ic_refresh_24dp)
            iconSize = mContext.resources.getDimensionPixelSize(dimen.base_dp24)
            iconTint = null
            iconPadding = mContext.resources.getDimensionPixelSize(dimen.base_dp6)
            text = mContext.getString(R.string.home_refresh)
        }
    }

    private fun showHomePage() {

        // 通知每一个 适配器 范围更改
        mHomeBannerAdapter.notifyItemRangeChanged(0, mHomeBannerAdapter.bannerList.size - 1)
        mHomeRecAdapter.notifyItemRangeChanged(0, mHomeRecAdapter.getDataSize())
        mHomeHotAdapter.notifyItemRangeChanged(0, mHomeHotAdapter.getDataSize())
        mHomeNewAdapter.notifyItemRangeChanged(0, mHomeNewAdapter.getDataSize())
        mHomeCommitAdapter.notifyItemRangeChanged(0, mHomeCommitAdapter.getDataSize())
        mHomeTopicAapter.notifyItemRangeChanged(0, mHomeTopicAapter.getDataSize())
        mHomeRankAapter.notifyItemRangeChanged(0, mHomeRankAapter.getDataSize())

        // 刷新事件 为空则 执行淡入动画（代表第一次加载进入布局）
        if (mRefreshListener == null) mBinding.homeLinearLayout.animateFadeIn(300L)

        mRefreshListener?.let {
            it.onRefresh()
            toast("刷新成功~")
        }
    }

    private fun doOnLoadHomePage(results: Results) {
        mHomeBannerAdapter.bannerList.clear()
        mHomeBannerAdapter.bannerList.addAll(results.mBanners.filter { banner -> banner.mType <= 2 })
        mHomeRecAdapter.setData(results.mRecComicsResult, 3)
        mHomeHotAdapter.setData(results.mHotComics, 12)
        mHomeNewAdapter.setData(results.mNewComics, 12)
        mHomeCommitAdapter.setData(results.mFinishComicDatas, 6)
        mHomeTopicAapter.setData(results.mTopics, 4)
        mHomeRankAapter.setData(results.mRankDayComics, 6)
    }

    fun doOnRefresh(refreshListener: OnRefreshListener) {
        mRefreshListener = refreshListener
        mHomeVM.input(HomeIntent.GetHomePage())
    }
}