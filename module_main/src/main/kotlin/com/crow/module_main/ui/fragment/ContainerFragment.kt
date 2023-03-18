package com.crow.module_main.ui.fragment

import android.view.LayoutInflater
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


    private lateinit var mContainerAdapter: ContainerAdapter
    private val mContaienrVM by viewModel<ContainerViewModel>()

    // AppBar搜索
    private var mAppBarState: Int = STATE_EXPANDED

    // 碎片集
    private val mFragmentList by lazy { mutableListOf<Fragment>(HomeFragment(mTapComicListener), DiscoveryFragment(), BookShelfFragment()) }

    // 点击标志 用于防止多次显示 ComicInfoBottomSheetFragment
    private var mTapFlag: Boolean = false

    // 事件层级: ContainerFragment --> HomeFragment --> HomeBookAdapter --> HomeFragment --> ContainerFragment
    private val mTapComicListener = object : HomeFragment.TapComicListener {
        override fun onTap(type: ComicType, pathword: String) {
            if (mTapFlag) return
            mTapFlag = true
            ComicInfoBottomSheetFragment(pathword, true).show(parentFragmentManager, ComicInfoBottomSheetFragment.TAG)
            this@ContainerFragment.doAfterDelay(1000L) { mTapFlag = false }
        }
    }

    override fun getViewBinding(inflater: LayoutInflater) = MainFragmentContainerBinding.inflate(inflater)

    override fun initView() {

        // 重新创建View之后 appBarLayout会展开折叠，记录一个状态进行初始化
        if (mAppBarState == STATE_COLLAPSED) mBinding.mainContaienrAppbar.setExpanded(false, false)
        else mBinding.mainContaienrAppbar.setExpanded(true, false)

        // 关联 SearchBar ＆ SearchView
        mBinding.mainSearchView.setupWithSearchBar(mBinding.mainContainerSearchBar)

        // 适配器 初始化 （设置Adapter、预加载页数）
        mContainerAdapter = ContainerAdapter(mFragmentList, requireActivity().supportFragmentManager, viewLifecycleOwner.lifecycle)
        mBinding.mainViewPager.adapter = mContainerAdapter
        mBinding.mainViewPager.offscreenPageLimit = 1

        // 设置刷新控件的的内部颜色
        mBinding.mainRefresh.setColorSchemeColors(ContextCompat.getColor(mContext, R.color.main_light_blue))

        // 关联 TabLayout & ViewPager2
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

    override fun initListener() {

        // SearchBar 点击监听
        mBinding.mainContainerSearchBar.clickGap { _, _ -> mBinding.mainSearchView.show() }

        // 记录AppBar的状态 （展开、折叠）偏移监听
        mBinding.mainContaienrAppbar.addOnOffsetChangedListener { appBar, vtOffSet ->
            mAppBarState = if (vtOffSet == 0) STATE_EXPANDED else if(abs(vtOffSet) >= appBar.totalScrollRange) STATE_COLLAPSED else STATE_COLLAPSED
        }

        // ToolBar 索引0 （设置）点击监听
        mBinding.mainContaienrToolbar.menu[0].clickGap { _, _ ->
            val dialog = MaterialAlertDialogBuilder(mContext)
            dialog.setTitle("拷贝漫画")
            dialog.setPositiveButton("知道了~", null)
            dialog.show()
        }

        // 刷新监听
        mBinding.mainRefresh.setAutoCancelRefreshing(viewLifecycleOwner) {
            when(mBinding.mainViewPager.currentItem) {
                0 -> (mFragmentList[0] as HomeFragment).doOnRefresh(mBinding.mainRefresh)
                1 -> { }
                2 -> { }
            }
        }
    }
}