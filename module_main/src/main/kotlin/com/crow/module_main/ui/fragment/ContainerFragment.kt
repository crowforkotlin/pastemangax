package com.crow.module_main.ui.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.crow.base.copymanga.BaseStrings
import com.crow.base.copymanga.BaseUser
import com.crow.base.tools.coroutine.FlowBus
import com.crow.base.tools.extensions.onCollect
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.viewmodel.doOnErrorInCoroutine
import com.crow.base.ui.viewmodel.doOnResultInCoroutine
import com.crow.module_bookshelf.ui.fragment.BookshelfFragment
import com.crow.module_discover.ui.fragment.DiscoverComicFragment
import com.crow.module_discover.ui.fragment.DiscoverNovelFragment
import com.crow.module_home.ui.fragment.HomeFragment
import com.crow.module_main.R
import com.crow.module_main.databinding.MainFragmentContainerBinding
import com.crow.module_main.model.intent.ContainerIntent
import com.crow.module_main.ui.adapter.ContainerAdapter
import com.crow.module_main.ui.viewmodel.ContainerViewModel
import com.crow.module_user.ui.viewmodel.UserViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_home/src/main/kotlin/com/crow/module_home/ui/fragment
 * @Time: 2023/3/7 14:00
 * @Author: CrowForKotlin
 * @Description: HomeContainerFragment
 * @formatter:on
 **************************/
class ContainerFragment : BaseMviFragment<MainFragmentContainerBinding>() {

    // FlowBus Init
    init {
        FlowBus.with<Unit>(BaseStrings.Key.CLEAR_USER_INFO).register(this) { mUserVM.doClearUserInfo() }                                       // 清除用户数据
        FlowBus.with<Unit>(BaseStrings.Key.LOGIN_SUCUESS).register(this) { doLoginSuccessRefresh() }                                          // 登录成功后响应回来进行刷新
        FlowBus.with<Unit>(BaseStrings.Key.EXIT_USER).register(this) { doExitUser() }                                                                           // 退出账号
    }

    // 碎片容器适配器
    private var mContainerAdapter: ContainerAdapter? = null

    // 容器VM
    private val mContainerVM by sharedViewModel<ContainerViewModel>()

    // 用户VM
    private val mUserVM by sharedViewModel<UserViewModel>()

    // 碎片集
    private val mFragmentList by lazy { mutableListOf<Fragment>(HomeFragment.newInstance(), DiscoverComicFragment.newInstance(), DiscoverNovelFragment(),BookshelfFragment.newInstance()) }

    override fun getViewBinding(inflater: LayoutInflater) = MainFragmentContainerBinding.inflate(inflater)

    override fun initObserver() {

        // 用户信息 收集
        mUserVM.userInfo.onCollect(this) {

            // 加载 Icon  无链接或加载失败 则默认Drawable
            mUserVM.doLoadIcon(mContext, true) { resource -> FlowBus.with<Drawable>(BaseStrings.Key.SET_HOME_ICON).post(this, resource) }

            // 初始化 用户Tokne
            BaseUser.CURRENT_USER_TOKEN = it?.mToken ?: return@onCollect
        }

        // 观察ContainerVM
        mContainerVM.onOutput { intent ->
            when(intent) {
                is ContainerIntent.GetDynamicSite -> {
                    intent.mViewState
                        .doOnErrorInCoroutine { _, _ -> mContainerVM.saveAppConfig() }
                        .doOnResultInCoroutine {
                            BaseStrings.URL.CopyManga = Base64.decode(intent.siteResp!!.mSiteList!!.first()!!.mEncodeSite, Base64.DEFAULT).decodeToString()
                            mContainerVM.saveAppConfig()
                        }
                }
            }
        }
    }

    override fun initView(bundle: Bundle?) {

        // 适配器 初始化 （设置Adapter、预加载页数）
        mContainerAdapter = ContainerAdapter(mFragmentList, childFragmentManager, viewLifecycleOwner.lifecycle)
        mBinding.mainViewPager.adapter = mContainerAdapter
        mBinding.mainViewPager.offscreenPageLimit = 4
        mBinding.mainViewPager.isUserInputEnabled = false
    }

    // 检查更新
    override fun initData() { mContainerVM.input(ContainerIntent.GetUpdateInfo()) }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)

        // 不为隐藏 当返回ContainerFragment时回调此方法 则通知设置Icon
        if (!hidden) mUserVM.doLoadIcon(mContext, true) { resource -> FlowBus.with<Drawable>(BaseStrings.Key.SET_HOME_ICON).post(this, resource) }
    }

    override fun initListener() {

        // 设置底部导航视图点击Item可见
        mBinding.mainBottomNavigation.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.main_menu_homepage -> doSwitchFragment(0)
                R.id.main_menu_discovery_comic -> doSwitchFragment(1)
                R.id.main_menu_discovery_novel -> doSwitchFragment(2)
                R.id.main_menu_bookshelf -> doSwitchFragment(3)
            }
            true
        }
    }

    // 执行退出用户
    private fun doExitUser() {
        mUserVM.doClearUserInfo()
        (mFragmentList[3] as BookshelfFragment).doExitFromUser()
    }

    // 执行登陆成功刷新
    private fun doLoginSuccessRefresh() {
        (mFragmentList[0] as HomeFragment).doRefresh()
        (mFragmentList[3] as BookshelfFragment).doRefresh()
    }

    // 执行选择Fragment
    private fun doSwitchFragment(position: Int) {
        if (mBinding.mainViewPager.currentItem != position) mBinding.mainViewPager.setCurrentItem(position, true)
        FlowBus.with<Int>(BaseStrings.Key.POST_CURRENT_ITEM).post(lifecycleScope, position)
    }
}