package com.crow.module_main.ui.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.crow.base.current_project.BaseStrings
import com.crow.base.current_project.BaseUser
import com.crow.base.current_project.entity.BookTapEntity
import com.crow.base.tools.coroutine.FlowBus
import com.crow.base.tools.extensions.*
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.module_book.ui.fragment.BookInfoFragment
import com.crow.module_bookshelf.ui.fragment.BookshelfFragment
import com.crow.module_discover.ui.fragment.DiscoverFragment
import com.crow.module_home.ui.fragment.HomeFragment
import com.crow.module_main.R
import com.crow.module_main.databinding.MainFragmentContainerBinding
import com.crow.module_main.databinding.MainUpdateLayoutBinding
import com.crow.module_main.databinding.MainUpdateUrlLayoutBinding
import com.crow.module_main.model.intent.ContainerIntent
import com.crow.module_main.model.resp.MainAppUpdateResp
import com.crow.module_main.ui.adapter.ContainerAdapter
import com.crow.module_main.ui.adapter.MainAppUpdateRv
import com.crow.module_main.ui.viewmodel.ContainerViewModel
import com.crow.module_user.ui.fragment.UserBottomSheetFragment
import com.crow.module_user.ui.fragment.UserLoginFragment
import com.crow.module_user.ui.viewmodel.UserViewModel
import com.orhanobut.logger.Logger
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
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

    companion object { fun newInstance() = ContainerFragment() }

    init {
        FlowBus.with<BookTapEntity>(BaseStrings.Key.OPEN_BOOK_INFO).register(this) { doShowComicInfo(it) }  // 主页点击漫画
        FlowBus.with<String>(BaseStrings.Key.LOGIN_SUCUESS).register(this) { doLoginSuccessRefresh(it) }        // 登录成功后响应回来进行刷新
        FlowBus.with<Unit>(BaseStrings.Key.CLEAR_USER_INFO).register(this) { mUserVM.doClearUserInfo() }    // 清除用户数据
        FlowBus.with<Unit>(BaseStrings.Key.EXIT_USER).register(this) { doExitUser() }                       // 退出账号
        FlowBus.with<Unit>(BaseStrings.Key.OPEN_USER_BOTTOM).register(this) { doShowUserBottom() }          // 打开用户界面
        FlowBus.with<Unit>(BaseStrings.Key.OPEN_LOGIN_FRAGMENT).register(this) { doShowLoginFragment() }    // 打开登录界面
        FlowBus.with<Unit>(BaseStrings.Key.CHECK_UPDATE).register(this) { mContaienrVM.input(ContainerIntent.GetUpdateInfo()) } // 查询更新
    }

    // 碎片容器适配器
    private var mContainerAdapter: ContainerAdapter? = null

    // 容器VM
    private val mContaienrVM by viewModel<ContainerViewModel>()

    // 用户VM
    private val mUserVM by sharedViewModel<UserViewModel>()

    // 碎片集
    private val mFragmentList by lazy { mutableListOf<Fragment>(HomeFragment.newInstance(), DiscoverFragment.newInstance(), BookshelfFragment.newInstance()) }

    // 点击标志 用于防止多次显示 BookInfo 以及 UserBottom
    private var mTapBookFlag: Boolean = false
    private var mTapUserFlag: Boolean = false

    override fun getViewBinding(inflater: LayoutInflater) = MainFragmentContainerBinding.inflate(inflater)

    override fun initView(bundle: Bundle?) {

        // 适配器 初始化 （设置Adapter、预加载页数）
        mContainerAdapter = ContainerAdapter(mFragmentList, childFragmentManager, lifecycle)
        mBinding.mainViewPager.adapter = mContainerAdapter
        mBinding.mainViewPager.offscreenPageLimit = 3
        mBinding.mainViewPager.isUserInputEnabled = false
    }

    override fun initObserver() {

        // 用户信息 收集
        mUserVM.userInfo.onCollect(this) {

            // 加载 Icon  无链接或加载失败 则默认Drawable
            mUserVM.doLoadIcon(mContext, true) { resource -> FlowBus.with<Drawable>(BaseStrings.Key.SET_HOME_ICON).post(this, resource) }

            // 初始化 用户Tokne
            BaseUser.CURRENT_USER_TOKEN = it?.mToken ?: return@onCollect
        }

        mContaienrVM.onOutput { intent ->
            when(intent) {
                is ContainerIntent.GetUpdateInfo -> {
                    intent.mViewState.doOnResult { doUpdateChecker(intent.appUpdateResp!!) }
                }
            }
        }
    }

    override fun initData() {
        mContaienrVM.input(ContainerIntent.GetUpdateInfo())
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) mUserVM.doLoadIcon(mContext, true) { resource -> FlowBus.with<Drawable>(BaseStrings.Key.SET_HOME_ICON).post(this, resource) }
    }

    override fun initListener() {

        // 设置底部导航视图点击Item可见
        mBinding.mainBottomNavigation.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.main_menu_homepage -> doSwitchFragment(0)
                R.id.main_menu_discovery -> doSwitchFragment(1)
                R.id.main_menu_bookshelf -> doSwitchFragment(2)
            }
            true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
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

    private fun doShowUserBottom() {
        if (mTapUserFlag) return
        mTapUserFlag = true
        if (!mTapBookFlag) {
            UserBottomSheetFragment(
                hideOnFade = { fragment -> fragment.parentFragmentManager.hide(this, "ContainerFragment") },
                navigateAbout = {
                    parentFragmentManager.navigateByAddWithBackStack(baseR.id.app_main_fcv, AboutAuthorFragment.newInstance(), "AboutAuthorFragment") {
                        it.withFadeAnimation()
                    }
                }
            ).show(parentFragmentManager, UserBottomSheetFragment.TAG)
        }
        doAfterDelay(EventGapTime.BASE_FLAG_TIME) { mTapUserFlag = false }
    }

    private fun doExitUser() {
        mUserVM.doClearUserInfo()
        (mFragmentList[2] as BookshelfFragment).doRefresh()
    }

    private fun doShowComicInfo(bookTapEntity: BookTapEntity) {
        if (mTapBookFlag) return
        mTapBookFlag = true
        if (!mTapUserFlag) {
            val bundle = Bundle()
            bundle.putSerializable("tapEntity", bookTapEntity)
            parentFragmentManager.hide(this, "BookInfoFragment")
            parentFragmentManager.navigateByAddWithBackStack(baseR.id.app_main_fcv, BookInfoFragment.newInstance().also { fragment -> fragment.arguments = bundle }, "BookInfoFragment")
        }
        doAfterDelay(EventGapTime.BASE_FLAG_TIME) { mTapBookFlag = false }
    }

    private fun doLoginSuccessRefresh(msg: String) {
        (mFragmentList[0] as HomeFragment).doRefresh()
        (mFragmentList[2] as BookshelfFragment).doRefresh(msg)
    }

    private fun doSwitchFragment(position: Int) {
        if (mBinding.mainViewPager.currentItem != position) mBinding.mainViewPager.setCurrentItem(position, true)
        FlowBus.with<Int>(BaseStrings.Key.POST_CURRENT_ITEM).post(lifecycleScope, position)
    }

    private fun doShowLoginFragment() {
        parentFragmentManager.hide(this, "ContainerFragment")
        parentFragmentManager.navigateByAddWithBackStack(baseR.id.app_main_fcv, UserLoginFragment.newInstance(), "UserLoginFragment")
    }

    private fun doUpdateChecker(appUpdateResp: MainAppUpdateResp) {
        val update = appUpdateResp.mUpdates.first()
        if (!isLatestVersion(latest = update.mVersionCode.toLong())) return
        val updateBd = MainUpdateLayoutBinding.inflate(layoutInflater)
        val updateDialog = mContext.newMaterialDialog { dialog ->
            dialog.setCancelable(false)
            dialog.setView(updateBd.root)
        }
        val screenHeight = mContext.resources.displayMetrics.heightPixels / 3
        (updateBd.mainUpdateScrollview.layoutParams as ConstraintLayout.LayoutParams).matchConstraintMaxHeight = screenHeight
        updateBd.mainUpdateCancel.isInvisible = appUpdateResp.mForceUpdate
        updateBd.mainUpdateTitle.text = update.mTitle
        updateBd.mainUpdateText.text = update.mContent
        updateBd.mainUpdateTime.text = getString(R.string.main_update_time, update.mTime)
        if (!appUpdateResp.mForceUpdate) { updateBd.mainUpdateCancel.clickGap { _, _ -> updateDialog.dismiss() } }
        updateBd.mainUpdateGo.clickGap { _, _ ->
            updateDialog.dismiss()
            val updateUrlBd = MainUpdateUrlLayoutBinding.inflate(layoutInflater)
            val updateUrlDialog = mContext.newMaterialDialog {
                it.setCancelable(false)
                it.setView(updateUrlBd.root)
            }
            (updateUrlBd.mainUpdateUrlScrollview.layoutParams as ConstraintLayout.LayoutParams).matchConstraintMaxHeight = screenHeight
            updateUrlBd.mainUpdateUrlCancel.isInvisible = appUpdateResp.mForceUpdate
            updateUrlBd.mainUpdateUrlRv.adapter = MainAppUpdateRv(update.mUrl)
            if (!appUpdateResp.mForceUpdate) { updateUrlBd.mainUpdateUrlCancel.clickGap { _, _ -> updateUrlDialog.dismiss() } }
        }
    }
}