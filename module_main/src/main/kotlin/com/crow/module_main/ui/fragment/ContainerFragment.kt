package com.crow.module_main.ui.fragment

import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.crow.base.current_project.BaseStrings
import com.crow.base.current_project.BaseUser
import com.crow.base.tools.coroutine.FlowBus
import com.crow.base.tools.extensions.*
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.module_bookshelf.ui.fragment.BookshelfFragment
import com.crow.module_comic.ui.fragment.ComicInfoBottomSheetFragment
import com.crow.module_discovery.ui.fragment.DiscoveryFragment
import com.crow.module_home.model.entity.ComicTapEntity
import com.crow.module_home.ui.fragment.HomeFragment
import com.crow.module_main.R
import com.crow.module_main.databinding.MainFragmentContainerBinding
import com.crow.module_main.ui.adapter.ContainerAdapter
import com.crow.module_main.ui.viewmodel.ContainerViewModel
import com.crow.module_user.ui.fragment.UserBottomSheetFragment
import com.crow.module_user.ui.viewmodel.UserViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_home/src/main/kotlin/com/crow/module_home/ui/fragment
 * @Time: 2023/3/7 14:00
 * @Author: CrowForKotlin
 * @Description: HomeContainerFragment
 * @formatter:on
 **************************/
class ContainerFragment : BaseMviFragment<MainFragmentContainerBinding>() {

    companion object {
        const val LOGIN_SCUESS = 1
    }

    init {

        // 登录成功后响应回来进行刷新
        FlowBus.with<Unit>(BaseStrings.Key.LOGIN_SUCUESS).register(this) {
            (mFragmentList[0] as HomeFragment).doRefresh()
            (mFragmentList[2] as BookshelfFragment).doRefresh()
        }

        // 主页点击漫画
        FlowBus.with<ComicTapEntity>(BaseStrings.Key.HOME_COMIC_TAP).register(this) {
            if (mTapFlag) return@register
            mTapFlag = true
            ComicInfoBottomSheetFragment(it.pathword, true).show(parentFragmentManager, ComicInfoBottomSheetFragment.TAG)
            doAfterDelay(EventGapTime.BASE_FLAG_TIME) { mTapFlag = false }
        }

        // 清除用户数据
        FlowBus.with<Unit>(BaseStrings.Key.CLEAR_USER_INFO).register(this) { mUserVM.doClearUserInfo() }

        // 退出账号
        FlowBus.with<Unit>(BaseStrings.Key.EXIT_USER).register(this) {
            mUserVM.doClearUserInfo()
            if (mBinding.mainViewPager.currentItem == 2) (mFragmentList[2] as BookshelfFragment).doRefresh()
        }

        // 打开用户界面
        FlowBus.with<Unit>(BaseStrings.Key.OPEN_USER_BOTTOM).register(this) {
            UserBottomSheetFragment().show(requireActivity().supportFragmentManager, UserBottomSheetFragment.TAG)
        }
    }

    // 碎片容器适配器
    private lateinit var mContainerAdapter: ContainerAdapter

    // 容器VM
    private val mContaienrVM by viewModel<ContainerViewModel>()

    // 共享用户VM
    private val mUserVM by sharedViewModel<UserViewModel>()

    // 碎片集
    private val mFragmentList by lazy { mutableListOf<Fragment>(HomeFragment(), DiscoveryFragment(), BookshelfFragment()) }

    // 点击标志 用于防止多次显示 ComicInfoBottomSheetFragment
    private var mTapFlag: Boolean = false

    override fun getViewBinding(inflater: LayoutInflater) = MainFragmentContainerBinding.inflate(inflater)

    override fun initListener() {
        // SearchBar 点击监听
        // mBinding.mainContainerSearchBar.clickGap { _, _ -> mBinding.mainSearchView.show() }
    }

    override fun initView() {

        // 设置 内边距属性 实现沉浸式效果
        mBinding.root.setPadding(0, mContext.getStatusBarHeight(), 0, 0)


        // 适配器 初始化 （设置Adapter、预加载页数）
        mContainerAdapter = ContainerAdapter(mFragmentList, requireActivity().supportFragmentManager, viewLifecycleOwner.lifecycle)
        mBinding.mainViewPager.adapter = mContainerAdapter
        mBinding.mainViewPager.offscreenPageLimit = 3
        mBinding.mainViewPager.isUserInputEnabled = false

        // 设置底部导航视图点击Itemhi见
        mBinding.mainBottomNavigation.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.main_menu_homepage -> switchFragment(0)
                R.id.main_menu_discovery -> switchFragment(1)
                R.id.main_menu_bookshelf -> switchFragment(2)
            }
            true
        }


        mBinding.mainViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                state.logMsg()
                if (state != ViewPager2.SCROLL_STATE_IDLE) return
                mBinding.mainBottomNavigation.selectedItemId = when(mBinding.mainViewPager.currentItem) {
                    0 -> R.id.main_menu_homepage
                    1 -> R.id.main_menu_discovery
                    2 -> R.id.main_menu_bookshelf
                    else -> return
                }
            }
        })

        // 关联 TabLayout & ViewPager2
        /*TabLayoutMediator(mBinding.mainContainerTabLayout, mBinding.mainViewPager) { tab, pos ->
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
        }.attach()*/


    }

    override fun initObserver() {

        // 用户信息 收集
        mUserVM.userInfo.onCollect(this) {

            // 加载 Icon  无链接或加载失败 则默认Drawable
            mUserVM.doLoadIcon(mContext, true) { resource ->  (mFragmentList[0] as HomeFragment).setIconResource(resource) }

            // 初始化 用户Tokne
            BaseUser.CURRENT_USER_TOKEN = it?.mToken ?: return@onCollect
        }
    }

    private fun switchFragment(position: Int) {
        if (mBinding.mainViewPager.currentItem != position) {
            mBinding.mainViewPager.setCurrentItem(position, true)
        }
    }
}