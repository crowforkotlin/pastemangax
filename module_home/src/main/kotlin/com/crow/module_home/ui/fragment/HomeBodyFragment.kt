package com.crow.module_home.ui.fragment

import android.annotation.SuppressLint
import android.view.LayoutInflater
import com.crow.base.fragment.BaseVBFragment
import com.crow.module_home.databinding.HomeFragmentBodyBinding
import com.crow.module_home.ui.HomeViewModel
import com.crow.module_home.ui.adapter.HomeBannerAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel


/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_home/src/main/kotlin/com/crow/module_home/view
 * @Time: 2023/3/6 0:14
 * @Author: BarryAllen
 * @Description: HomeBodyFragment
 * @formatter:on
 **************************/
class HomeBodyFragment : BaseVBFragment<HomeFragmentBodyBinding, HomeViewModel>() {

    private val mContext by lazy { requireContext() }
    private val mHomeBannerAdapter = HomeBannerAdapter(mutableListOf()) { _, _ ->

    }

    override fun getViewBinding(inflater: LayoutInflater) = HomeFragmentBodyBinding.inflate(inflater)

    override fun getViewModel(): Lazy<HomeViewModel> = viewModel()
    override fun initData() {
        mViewModel.getHomePage()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun initObserver() {
        super.initObserver()
        mViewModel.mHomePageResp.observe(this) { datas ->
            mHomeBannerAdapter.bannerList.clear()
            mHomeBannerAdapter.bannerList.addAll(datas.mResults.mBanners.filter { it.mType <= 2 })
            mHomeBannerAdapter.notifyDataSetChanged()
        }
    }

    override fun initView() {

        mBinding.homeSearchView.setupWithSearchBar(mBinding.homeSearchBar)

        mBinding.homeSearchBar.setOnClickListener {
            mBinding.homeSearchView.show()
        }

        /*mBinding.homeBodyBanner.apply {
            viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    layoutParams.height = (width / 1.875 + 0.5).toInt()
                    invalidate()
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
        }

        mBinding.homeBodyBanner
            .addPageTransformer(ScaleInTransformer())
            .setPageMargin(mContext.dp2px(20), mContext.dp2px(10))
            .setIndicator(
                IndicatorView(mContext)
                    .setIndicatorColor(Color.DKGRAY)
                    .setIndicatorSelectorColor(Color.WHITE)
                    .setIndicatorStyle(IndicatorView.IndicatorStyle.INDICATOR_BEZIER)
                    .also {
                        it.doOnLayout { view ->
                            (view.layoutParams as RelativeLayout.LayoutParams).bottomMargin = mContext.resources.getDimensionPixelSize(R.dimen.base_dp20)
                        }
                    }
            )
            .adapter = mHomeBannerAdapter*/
    }
}