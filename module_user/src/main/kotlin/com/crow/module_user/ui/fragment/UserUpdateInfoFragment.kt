package com.crow.module_user.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.addCallback
import androidx.lifecycle.lifecycleScope
import com.crow.base.current_project.BaseStrings
import com.crow.base.current_project.entity.Fragments
import com.crow.base.current_project.processTokenError
import com.crow.base.tools.coroutine.FlowBus
import com.crow.base.tools.extensions.*
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnLoading
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.module_user.R
import com.crow.module_user.databinding.UserFragmentInfoBinding
import com.crow.module_user.model.UserIntent
import com.crow.module_user.ui.adapter.UserUpdateInfoRvAdapter
import com.crow.module_user.ui.viewmodel.UserInfoViewModel
import com.crow.module_user.ui.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.crow.base.R as baseR


/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_user/src/main/kotlin/com/crow/module_user/ui/fragment
 * @Time: 2023/3/20 23:02
 * @Author: CrowForKotlin
 * @Description: UserInfoFragment
 * @formatter:on
 **************************/
class UserUpdateInfoFragment : BaseMviFragment<UserFragmentInfoBinding>() {

    // 共享用户VM
    private val mUserVM by sharedViewModel<UserViewModel>()

    // 用户 更新信息 VM
    private val mUserUpdateInfoVM by viewModel<UserInfoViewModel>()

    // 用户更新信息适配器
    private var mUserUpdateInfoRvAdapter: UserUpdateInfoRvAdapter? = null

    // 手动退出标志位
    private var mExitFragment = false

    private fun navigateUp() {
        parentFragmentManager.popSyncWithClear(Fragments.UserInfo.toString())
        mUserUpdateInfoVM.doClearUserUpdateInfoData()
    }

    override fun onStart() {
        super.onStart()
        mBackDispatcher = requireActivity().onBackPressedDispatcher.addCallback(this) { navigateUp() }
    }

    override fun getViewBinding(inflater: LayoutInflater) = UserFragmentInfoBinding.inflate(inflater)

    override fun initView(bundle: Bundle?) {

        // 设置 内边距属性 实现沉浸式效果
        mBinding.root.setPadding(0, mContext.getStatusBarHeight(), 0, mContext.getNavigationBarHeight())

    }

    override fun initListener() {

        // 头像 点击事件
        mBinding.userUpdateInfoIcon.doOnClickInterval {
            parentFragmentManager.navigateToWithBackStack<UserIconFragment>(baseR.id.app_main_fcv, this, null, Fragments.Icon.toString(), Fragments.Icon.toString())
        }

        // 退出账号 点击事件
        mBinding.userUpdateInfoExitButton.doOnClickInterval { doExitFragment() }
    }

    override fun initObserver() {

        // 恢复 标志位
        mExitFragment = false

        // 用户信息 收集 因为是粘性直接收集即可初始化
        mUserVM.userInfo.onCollect(this) {

            // 判断是否退出Fragment
            if (mExitFragment) return@onCollect

            // 初始化 Icon链接 设置用户名 退出可见 修改适配器数据
            mUserVM.doLoadIcon(mContext, false) { resource -> mBinding.userUpdateInfoIcon.setImageDrawable(resource) }

            // 第一次UserInfo初始化NULL是会观察到的，需做判断设置完头像后退出
            if (it == null) return@onCollect

            // 初始化Rv
            mUserUpdateInfoRvAdapter = UserUpdateInfoRvAdapter(mUserUpdateInfoVM.mUserUpdateInfoData) { _, _ -> }

            // 获取VM的数据 为空则发请求
            if (mUserUpdateInfoVM.mUserUpdateInfoData.isEmpty()) mUserVM.input(UserIntent.GetUserUpdateInfo())

            // itemCount == 0 则设置数据
            if (mUserUpdateInfoRvAdapter!!.itemCount == 0) mUserUpdateInfoVM.setData(it ?: return@onCollect)

            // 设置InfoRv适配器
            mBinding.userUpdateInfoRv.adapter = mUserUpdateInfoRvAdapter
        }

        mUserVM.onOutput { intent ->
            if (intent is UserIntent.GetUserUpdateInfo) {
                intent.mViewState
                    .doOnLoading { showLoadingAnim() }
                    .doOnError { code, msg ->
                        dismissLoadingAnim {
                            // 处理 Token错误的信息
                            mBinding.root.processTokenError(code, msg,
                                doOnCancel =  { doExitFragment() },
                                doOnConfirm = { doExitFragment(true) })
                        }
                    }
                    .doOnResult {
                        dismissLoadingAnim {
                            if (intent.userUpdateInfoResp == null) {
                                toast(getString(baseR.string.BaseUnknowError))
                                return@dismissLoadingAnim
                            }

                            // 设置 InfoVM的数据
                            mUserUpdateInfoVM.setData(intent.userUpdateInfoResp.mInfo)

                            // 更新适配器
                            viewLifecycleOwner.lifecycleScope.launch { mUserUpdateInfoRvAdapter?.doNotify() }
                        }
                    }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mUserUpdateInfoRvAdapter = null
    }

    private fun doExitFragment(isNeedNavigateLogin: Boolean = false) {
        viewLifecycleOwner.lifecycleScope.launch {

            // 退出Fragment 标志位 true
            mExitFragment = true

            // 发送事件清除用户全部数据
            FlowBus.with<Unit>(BaseStrings.Key.EXIT_USER).post(lifecycleScope, Unit)

            // 清除当前界面的用户数据
            mUserUpdateInfoVM.doClearUserUpdateInfoData()

            // SnackBar提示
            mBinding.root.showSnackBar(getString(R.string.user_exit_sucess))

            // 返回上一个界面
            navigateUp()

            // 为true则 深链跳转至登录界面
            if (isNeedNavigateLogin) {
                // parentFragmentManager.remove(this@UserUpdateInfoFragment)
                // parentFragmentManager.navigateByAddWithBackStack(baseR.id.app_main_fcv, UserLoginFragment.newInstance(), "UserLoginFragment")
            }
        }
    }
}