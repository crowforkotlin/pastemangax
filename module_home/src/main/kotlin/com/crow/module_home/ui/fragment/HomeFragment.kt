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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.crow.base.R.dimen
import com.crow.base.extensions.animateFadeIn
import com.crow.base.extensions.dp2px
import com.crow.base.extensions.repeatOnLifecycle
import com.crow.base.fragment.BaseMviFragment
import com.crow.base.viewmodel.ViewState
import com.crow.base.viewmodel.doOnError
import com.crow.base.viewmodel.doOnLoading
import com.crow.base.viewmodel.doOnSuccess
import com.crow.module_home.R
import com.crow.module_home.databinding.HomeFragmentBinding
import com.crow.module_home.databinding.HomeRvItemLayoutBinding
import com.crow.module_home.model.ComicType
import com.crow.module_home.model.intent.HomeEvent
import com.crow.module_home.model.resp.homepage.*
import com.crow.module_home.model.resp.homepage.results.RecComicsResult
import com.crow.module_home.model.resp.homepage.results.Results
import com.crow.module_home.ui.adapter.HomeBannerAdapter
import com.crow.module_home.ui.adapter.HomeBookAdapter
import com.crow.module_home.ui.fragment.HomeFragment.ClickComicListener
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

    constructor(clickListener: ClickComicListener? = null) : this() {
        mClickComicListener = clickListener ?: ClickComicListener { }
    }

    private var mClickComicListener: ClickComicListener? = null

    fun interface ClickComicListener {
        fun onClick(type: ComicType)
    }

    private val mViewModel by viewModel<HomeViewModel>()
    private val mHomeBannerAdapter = HomeBannerAdapter(mutableListOf()) { _, _ -> }
    private val mHomeRecAdapter = HomeBookAdapter<ComicDatas<RecComicsResult>>(null, ComicType.Rec, mClickComicListener)
    private val mHomeHotAdapter = HomeBookAdapter<List<HotComic>>(null, ComicType.Hot, mClickComicListener)
    private val mHomeNewAdapter = HomeBookAdapter<List<NewComic>>(null, ComicType.New, mClickComicListener)
    private val mHomeCommitAdapter = HomeBookAdapter<FinishComicDatas>(null, ComicType.Commit, mClickComicListener)
    private val mHomeTopicAapter = HomeBookAdapter<ComicDatas<Topices>>(null, ComicType.Topic, mClickComicListener)
    private val mHomeRankAapter = HomeBookAdapter<ComicDatas<RankComics>>(null, ComicType.Rank, mClickComicListener)

    private val mRefreshButton by lazy { initRefreshButton() }

    override fun getViewBinding(inflater: LayoutInflater) = HomeFragmentBinding.inflate(inflater)

    override fun initData() {
        mViewModel.input(HomeEvent.GetHomePage())
    }

    override fun initObserver() {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            mViewModel.output { event ->
                when (event) {
                    is HomeEvent.GetHomePage -> {
                        event.mViewState
                            .doOnLoading { showLoadingAnim() }
                            .doOnError { _, _ -> dismissLoadingAnim { mBinding.root.animateFadeIn() } }
                            .doOnSuccess {
                                if (it == ViewState.Success.ATTACH_VALUE) doOnLoadHomePage(event.homePageData!!.mResults)
                                else dismissLoadingAnim { lifecycleScope.launch { showHomePage() } }
                            }
                    }
                    is HomeEvent.GetRecPageByRefresh -> {
                        event.mViewState
                            .doOnSuccess {
                                if (it == ViewState.Success.ATTACH_VALUE) {
                                    mHomeRecAdapter.setData(event.recPageData!!.mResults)
                                    mHomeRecAdapter.notifyItemRangeChanged(
                                        0,
                                        mHomeRecAdapter.getUpdateSize()
                                    )
                                    mRefreshButton.isEnabled = true
                                }
                            }
                            .doOnError { _, _ ->
                                mRefreshButton.isEnabled = true
                            }
                    }
                }
            }
        }
    }

    private fun showHomePage() {
        lifecycleScope.launch {
            mBinding.root.animateFadeIn()
            mHomeBannerAdapter.notifyItemRangeChanged(0, mHomeBannerAdapter.bannerList.size - 1)
            delay(200L)
            mHomeRecAdapter.notifyItemRangeChanged(0, mHomeRecAdapter.getUpdateSize())
            delay(200L)
            mHomeHotAdapter.notifyItemRangeChanged(0, mHomeHotAdapter.getUpdateSize())
            delay(200L)
            mHomeNewAdapter.notifyItemRangeChanged(0, mHomeNewAdapter.getUpdateSize())
            delay(200L)
            mHomeCommitAdapter.notifyItemRangeChanged(0, mHomeCommitAdapter.getUpdateSize())
            delay(200L)
            mHomeTopicAapter.notifyItemRangeChanged(0, mHomeTopicAapter.getUpdateSize())
            delay(200L)
            mHomeRankAapter.notifyItemRangeChanged(0, mHomeRankAapter.getUpdateSize())
        }
    }

    override fun initView() {
        mBinding.homeBanner.doOnLayout { it.layoutParams.height = (it.width / 1.875 + 0.5).toInt() }
        mBinding.homeBanner.addPageTransformer(ScaleInTransformer())
            .setPageMargin(mContext.dp2px(20), mContext.dp2px(10))
            .setIndicator(
                IndicatorView(mContext)
                    .setIndicatorColor(Color.DKGRAY)
                    .setIndicatorSelectorColor(Color.WHITE)
                    .setIndicatorStyle(IndicatorView.IndicatorStyle.INDICATOR_BEZIER)
                    .also {
                        it.doOnLayout { view ->
                            (view.layoutParams as RelativeLayout.LayoutParams).bottomMargin =
                                mContext.resources.getDimensionPixelSize(dimen.base_dp20)
                        }
                    })
            .adapter = mHomeBannerAdapter
        mBinding.homeItemRec.initHomeItem(
            R.drawable.home_ic_recommed_24dp,
            R.string.home_recommend_comic,
            mHomeRecAdapter
        ).also { it.homeItemConstraint.addView(mRefreshButton) }
        mBinding.homeItemHot.initHomeItem(
            R.drawable.home_ic_hot_24dp,
            R.string.home_hot_comic,
            mHomeHotAdapter
        )
        mBinding.homeItemNew.initHomeItem(
            R.drawable.home_ic_new_24dp,
            R.string.home_new_comic,
            mHomeNewAdapter
        )
        mBinding.homeItemCommit.initHomeItem(
            R.drawable.home_ic_commit_24dp,
            R.string.home_commit_comic,
            mHomeCommitAdapter
        )
        mBinding.homeItemTopic.initHomeItem(
            R.drawable.home_ic_topic_24dp,
            R.string.home_topic_comic,
            mHomeTopicAapter
        ).also {
            it.homeItemBookRv.layoutManager = GridLayoutManager(mContext, 2)
        }
        mBinding.homeItemRank.initHomeItem(
            R.drawable.home_ic_rank_24dp,
            R.string.home_rank_comic,
            mHomeRankAapter
        )
    }

    private fun <T> HomeRvItemLayoutBinding.initHomeItem(@DrawableRes iconRes: Int, @StringRes iconText: Int, adapter: HomeBookAdapter<T>, ): HomeRvItemLayoutBinding {
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
            setOnClickListener {
                isEnabled = false
                mViewModel.input(HomeEvent.GetRecPageByRefresh())
            }
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
}