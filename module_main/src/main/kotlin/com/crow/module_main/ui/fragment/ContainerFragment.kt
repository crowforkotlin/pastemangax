package com.crow.module_main.ui.fragment

import android.view.LayoutInflater
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.crow.base.extensions.clickGap
import com.crow.base.extensions.doAfterDelay
import com.crow.base.extensions.setAutoCancelRefreshing
import com.crow.base.fragment.BaseMviFragment
import com.crow.module_bookshelf.BookShelfFragment
import com.crow.module_comic.ui.fragment.ComicInfoBottomSheetFragment
import com.crow.module_discovery.DiscoveryFragment
import com.crow.module_home.model.ComicType
import com.crow.module_home.ui.fragment.HomeFragment
import com.crow.module_main.R
import com.crow.module_main.databinding.MainFragmentContainerBinding
import com.crow.module_main.ui.adapter.ContainerAdapter
import com.crow.module_main.ui.viewmodel.ContainerViewModel
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.abs


/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_home/src/main/kotlin/com/crow/module_home/ui/fragment
 * @Time: 2023/3/7 14:00
 * @Author: CrowForKotlin
 * @Description: HomeContainerFragment
 * @formatter:on
 **************************/
class ContainerFragment : BaseMviFragment<MainFragmentContainerBinding>() {


    //折叠状态
    private var mCollapseState: Int = STATE_EXPANDED
    private lateinit var mOnBackCallback: OnBackPressedCallback
    private lateinit var mContainerAdapter: ContainerAdapter
    private val mContaienrVM by viewModel<ContainerViewModel>()
    private val fragmentList by lazy { mutableListOf<Fragment>(HomeFragment(mClickComicListener), DiscoveryFragment(), BookShelfFragment()) }
    private var mClickFlag: Boolean = false
    private val mClickComicListener = object : HomeFragment.ClickComicListener {
        override fun onClick(type: ComicType, pathword: String) {
            if (mClickFlag) return
            mClickFlag = true
            ComicInfoBottomSheetFragment(pathword).show(parentFragmentManager, ComicInfoBottomSheetFragment.TAG)
            this@ContainerFragment.doAfterDelay(1000L) { mClickFlag = false }
        }
    }

    override fun getViewBinding(inflater: LayoutInflater) = MainFragmentContainerBinding.inflate(inflater)

    override fun initView() {

        // 重新创建view之后 appBarLayout会展开折叠，记录一个状态进行初始化
        when (mCollapseState) {
            STATE_COLLAPSED -> mBinding.mainContaienrAppbar.setExpanded(false, false)
            else -> mBinding.mainContaienrAppbar.setExpanded(true, false)
        }

        mBinding.mainSearchView.setupWithSearchBar(mBinding.mainContainerSearchBar)

        mBinding.mainContainerSearchBar.setOnClickListener { mBinding.mainSearchView.show() }

        mBinding.mainContaienrToolbar.menu[0].clickGap { _, _ ->
            val dialog = MaterialAlertDialogBuilder(mContext)
            dialog.setTitle("拷贝漫画")
            dialog.setPositiveButton("知道了~", null)
            dialog.show()
        }

        mBinding.mainContaienrAppbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            mCollapseState = when {
                verticalOffset == 0 -> STATE_EXPANDED
                abs(verticalOffset) >= appBarLayout.totalScrollRange -> STATE_COLLAPSED
                else -> STATE_COLLAPSED
            }
        })

        mBinding.mainViewPager.apply {
            mContainerAdapter = ContainerAdapter(fragmentList, childFragmentManager, viewLifecycleOwner.lifecycle)
            adapter = mContainerAdapter
            offscreenPageLimit = 3
//            setPageTransformer(StereoPagerTransformer(mContext.resources.displayMetrics.widthPixels.toFloat()))
        }

        mBinding.mainRefresh.setColorSchemeColors(ContextCompat.getColor(mContext, R.color.main_light_blue))

        mBinding.mainRefresh.setAutoCancelRefreshing(viewLifecycleOwner) {
            when(mBinding.mainViewPager.currentItem) {
                0 -> (fragmentList[0] as HomeFragment).doOnRefresh { mBinding.mainRefresh.isRefreshing = false }
            }
        }

        TabLayoutMediator(mBinding.mainContainerTabLayout, mBinding.mainViewPager) { tab, pos ->
            when (pos) {
                0 -> {
                    tab.text = getString(com.crow.module_home.R.string.home_homepage)
                    tab.icon = ContextCompat.getDrawable(mContext, com.crow.module_home.R.drawable.home_ic_home_24dp)
                }
                1 -> {
                    tab.text = getString(com.crow.module_home.R.string.home_recovery)
                    tab.icon = ContextCompat.getDrawable(mContext, com.crow.module_home.R.drawable.home_ic_discovery_24dp)
                }
                2 -> {
                    tab.text = getString(com.crow.module_home.R.string.home_bookshelf)
                    tab.icon = ContextCompat.getDrawable(mContext, com.crow.module_home.R.drawable.home_ic_bookmark_24dp)
                }
                else -> { }
            }
        }.attach()
    }
}