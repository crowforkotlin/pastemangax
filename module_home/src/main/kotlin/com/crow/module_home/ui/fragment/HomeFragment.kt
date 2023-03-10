package com.crow.module_home.ui.fragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.widget.RelativeLayout
import androidx.core.view.doOnLayout
import com.crow.base.extensions.dp2px
import com.crow.base.fragment.BaseMviFragment
import com.crow.base.viewmodel.doOnResultWithLoading
import com.crow.module_home.databinding.HomeFragmentBinding
import com.crow.module_home.model.HomeEvent
import com.crow.module_home.ui.adapter.HomeBannerAdapter
import com.crow.module_home.ui.viewmodel.HomeViewModel
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
class HomeFragment : BaseMviFragment<HomeFragmentBinding>() {

    private val mHomeBannerAdapter = HomeBannerAdapter(mutableListOf()) { _, _ -> }
    private val mViewModel by viewModel<HomeViewModel>()

    override fun getViewBinding(inflater: LayoutInflater) = HomeFragmentBinding.inflate(inflater)


    override fun initData() {
        mViewModel.input(HomeEvent.GetHomePage())
    }


    @SuppressLint("NotifyDataSetChanged")
    override fun initObserver() {
        mViewModel.output { event ->
            when(event) {
                is HomeEvent.GetHomePage -> {
                    event.mViewState.doOnResultWithLoading(parentFragmentManager) {
                        val results = event.homePageData!!.mResults
                        mBinding.root.animateFadeIn()
                        mHomeBannerAdapter.bannerList.clear()
                        mHomeBannerAdapter.bannerList.addAll(results.mBanners.filter { banner -> banner.mType <= 2 })
                        mHomeBannerAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    override fun initView() {

        mBinding.homeBanner.apply {
            viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    layoutParams.height = (width / 1.875 + 0.5).toInt()
                    invalidate()
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
        }

        mBinding.homeBanner
            .addPageTransformer(ScaleInTransformer())
            .setPageMargin(mContext.dp2px(20), mContext.dp2px(10))
            .setIndicator(
                IndicatorView(mContext)
                    .setIndicatorColor(Color.DKGRAY)
                    .setIndicatorSelectorColor(Color.WHITE)
                    .setIndicatorStyle(IndicatorView.IndicatorStyle.INDICATOR_BEZIER)
                    .also {
                        it.doOnLayout { view ->
                            (view.layoutParams as RelativeLayout.LayoutParams).bottomMargin = mContext.resources.getDimensionPixelSize(
                                com.crow.base.R.dimen.base_dp20)
                        }
                    }
            )
            .adapter = mHomeBannerAdapter
    }

    // 启动动画淡入方式
    private fun View.animateFadeIn() {
        view.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate().alpha(1f).duration = 250L
        }
    }
}