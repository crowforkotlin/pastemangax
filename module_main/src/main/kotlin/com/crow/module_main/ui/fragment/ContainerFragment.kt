package com.crow.module_main.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.current_project.BaseStrings
import com.crow.base.current_project.BaseUser
import com.crow.base.current_project.entity.BookTapEntity
import com.crow.base.tools.coroutine.FlowBus
import com.crow.base.tools.extensions.*
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.module_bookshelf.ui.fragment.BookshelfFragment
import com.crow.module_discover.ui.fragment.DiscoverFragment
import com.crow.module_home.ui.fragment.HomeFragment
import com.crow.module_home.ui.fragment.NewHomeFragment
import com.crow.module_main.R
import com.crow.module_main.databinding.MainFragmentContainerBinding
import com.crow.module_main.ui.adapter.ContainerAdapter
import com.crow.module_main.ui.viewmodel.ContainerViewModel
import com.crow.module_user.ui.fragment.UserBottomSheetFragment
import com.crow.module_user.ui.viewmodel.UserViewModel
import com.orhanobut.logger.Logger
import kotlinx.coroutines.NonCancellable.children
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.logging.Level
import com.crow.base.R as baseR


/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_home/src/main/kotlin/com/crow/module_home/ui/fragment
 * @Time: 2023/3/7 14:00
 * @Author: CrowForKotlin
 * @Description: HomeContainerFragment
 * @formatter:on
 **************************/
class ContainerFragment : BaseMviFragment<MainFragmentContainerBinding>() {

    init {

        // 登录成功后响应回来进行刷新
        FlowBus.with<Unit>(BaseStrings.Key.LOGIN_SUCUESS).register(this) {
            (mFragmentList[0] as NewHomeFragment).doRefresh()
            (mFragmentList[2] as BookshelfFragment).doRefresh()
        }

        // 主页点击漫画
        FlowBus.with<BookTapEntity>(BaseStrings.Key.OPEN_BOOK_INFO).register(this) {
            if (mTapFlag) return@register
            mTapFlag = true
            navigate(baseR.id.mainBookinfofragment, Bundle().also { bundle -> bundle.putSerializable("tapEntity", it) })
            doAfterDelay(EventGapTime.BASE_FLAG_TIME) { mTapFlag = false }
        }

        // 清除用户数据
        FlowBus.with<Unit>(BaseStrings.Key.CLEAR_USER_INFO).register(this) { mUserVM.doClearUserInfo() }

        // 退出账号
        FlowBus.with<Unit>(BaseStrings.Key.EXIT_USER).register(this) {
            mUserVM.doClearUserInfo()
            (mFragmentList[2] as BookshelfFragment).doExitUser()
        }

        // 打开用户界面
        FlowBus.with<Unit>(BaseStrings.Key.OPEN_USER_BOTTOM).register(this) {
            UserBottomSheetFragment().show(requireActivity().supportFragmentManager, UserBottomSheetFragment.TAG)
        }
    }

    // 碎片容器适配器
    private var mContainerAdapter: ContainerAdapter? = null

    // 容器VM
    private val mContaienrVM by viewModel<ContainerViewModel>()

    // 共享用户VM
    private val mUserVM by viewModel<UserViewModel>()

    // 碎片集
    private val mFragmentList by lazy { mutableListOf<Fragment>(NewHomeFragment(), DiscoverFragment(), BookshelfFragment()) }

    // 点击标志 用于防止多次显示 ComicInfoBottomSheetFragment
    private var mTapFlag: Boolean = false



    private fun switchFragment(position: Int) {
        if (mBinding.mainViewPager.currentItem != position) {
            mBinding.mainViewPager.setCurrentItem(position, true)
        }
        FlowBus.with<Int>(BaseStrings.Key.POST_CURRENT_ITEM).post(lifecycleScope, position)
    }

    override fun getViewBinding(inflater: LayoutInflater) = MainFragmentContainerBinding.inflate(inflater)

    override fun initView() {

        "(Container Fragment) InitView Start".logMsg(Logger.WARN)

        // 适配器 初始化 （设置Adapter、预加载页数）
        mContainerAdapter = ContainerAdapter(mFragmentList, childFragmentManager, viewLifecycleOwner.lifecycle)
        mBinding.mainViewPager.adapter = mContainerAdapter
        mBinding.mainViewPager.offscreenPageLimit = 3
        mBinding.mainViewPager.isUserInputEnabled = false
        "(Container Fragment) InitView End".logMsg(Logger.WARN)

        // 设置底部导航视图点击Itemhi见
        mBinding.mainBottomNavigation.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.main_menu_homepage -> switchFragment(0)
                R.id.main_menu_discovery -> switchFragment(1)
                R.id.main_menu_bookshelf -> switchFragment(2)
            }
            true
        }
    }

    override fun initObserver() {

        // 用户信息 收集
        mUserVM.userInfo.onCollect(this) {

            // 加载 Icon  无链接或加载失败 则默认Drawable
            mUserVM.doLoadIcon(mContext, true) { resource ->  (mFragmentList[0] as NewHomeFragment).setIconResource(resource) }

            // 初始化 用户Tokne
            BaseUser.CURRENT_USER_TOKEN = it?.mToken ?: return@onCollect
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mContainerAdapter = null
        "(Container Fragment) onDestoryView".logMsg(Logger.WARN)
    }

    override fun onDestroy() {
        super.onDestroy()
        "(Container Fragment) onDestory".logMsg(Logger.WARN)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        "(Container Fragment) LowMemory".logMsg(Logger.WARN)
    }
}