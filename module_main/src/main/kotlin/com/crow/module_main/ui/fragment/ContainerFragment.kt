package com.crow.module_main.ui.fragment

import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.crow.base.current_project.BaseUser
import com.crow.base.tools.extensions.*
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.module_bookshelf.BookShelfFragment
import com.crow.module_comic.ui.fragment.ComicInfoBottomSheetFragment
import com.crow.module_discovery.DiscoveryFragment
import com.crow.module_home.model.ComicType
import com.crow.module_home.ui.fragment.HomeFragment
import com.crow.module_main.databinding.MainFragmentContainerBinding
import com.crow.module_main.ui.adapter.ContainerAdapter
import com.crow.module_main.ui.viewmodel.ContainerViewModel
import com.crow.module_user.ui.fragment.UserBottomSheetFragment
import com.crow.module_user.ui.viewmodel.UserViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
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

    // 碎片容器适配器
    private lateinit var mContainerAdapter: ContainerAdapter

    // 容器VM
    private val mContaienrVM by viewModel<ContainerViewModel>()

    // 共享用户VM
    private val mUserVM by sharedViewModel<UserViewModel>()

    // AppBar状态（默认展开）
    private var mAppBarState: Int = STATE_EXPANDED

    // 碎片集
    private val mFragmentList by lazy { mutableListOf<Fragment>(HomeFragment(mITapComicListener), DiscoveryFragment(), BookShelfFragment()) }

    // 点击标志 用于防止多次显示 ComicInfoBottomSheetFragment
    private var mTapFlag: Boolean = false

    // 事件层级: ContainerFragment --> HomeFragment --> HomeBookAdapter --> HomeFragment --> ContainerFragment
    private val mITapComicListener = object : HomeFragment.ITapComicListener {
        override fun onTap(type: ComicType, pathword: String) {
            if (mTapFlag) return
            mTapFlag = true
            ComicInfoBottomSheetFragment(pathword, true).show(parentFragmentManager, ComicInfoBottomSheetFragment.TAG)
            this@ContainerFragment.doAfterDelay(EventGapTime.BASE_FLAG_TIME) { mTapFlag = false }
        }
    }

    override fun getViewBinding(inflater: LayoutInflater) = MainFragmentContainerBinding.inflate(inflater)

    override fun initListener() {

        // MaterialToolBar NavigateIcon 点击事件
        mBinding.mainContaienrToolbar.navigateIconClickGap { _, _ -> UserBottomSheetFragment().show(requireActivity().supportFragmentManager, UserBottomSheetFragment.TAG) }

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

    override fun initView() {

        // 设置 内边距属性 实现沉浸式效果
        mBinding.root.setPadding(0, mContext.getStatusBarHeight(), 0, 0)

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
        mBinding.mainRefresh.setColorSchemeColors(ContextCompat.getColor(mContext, com.crow.module_main.R.color.main_light_blue))

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

    override fun initObserver() {

        // 用户信息 收集
        mUserVM.userInfo.onCollect(this) {

            // 加载 Icon  无链接或加载失败 则默认Drawable
            mUserVM.doLoadIcon(mContext, true) { resource ->  mBinding.mainContaienrToolbar.navigationIcon = resource }

            // 初始化 用户Tokne
            BaseUser.CURRENT_USER_TOKEN = it?.mToken ?: return@onCollect
        }
    }
}