package com.crow.module_user.ui.fragment

import android.view.LayoutInflater
import androidx.activity.OnBackPressedCallback
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.crow.base.app.appContext
import com.crow.base.current_project.updateLifecycleObserver
import com.crow.base.tools.extensions.*
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnLoading
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.base.ui.viewmodel.doOnSuccess
import com.crow.module_user.R
import com.crow.module_user.databinding.UserFragmentLoginBinding
import com.crow.module_user.model.UserIntent
import com.crow.module_user.ui.viewmodel.UserViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_user/src/main/kotlin/com/crow/module_user/ui/fragment
 * @Time: 2023/3/20 13:23
 * @Author: CrowForKotlin
 * @Description: UserLoginFragment
 * @formatter:on
 **************************/

class UserLoginFragment : BaseMviFragment<UserFragmentLoginBinding>(){

    // VM
    private val mUserVM by sharedViewModel<UserViewModel>()

    // 系统返回 事件回调
    private lateinit var mOnBackCallback: OnBackPressedCallback

    // 是否登录成功
    private var mIsLoginSuccess: Boolean = false

    override fun getViewBinding(inflater: LayoutInflater) = UserFragmentLoginBinding.inflate(inflater)

    override fun initObserver() {
        mUserVM.onOutput { intent ->
            when (intent) {
                // loading(加载动画) -> error(失败) 或 result(成功) -> success(取消动画)
                is UserIntent.Login -> {
                    intent.mViewState
                        .doOnLoading { showLoadingAnim() }
                        .doOnError { _, msg -> toast(msg ?: appContext.getString(com.crow.base.R.string.BaseUnknow), false) }
                        .doOnSuccess { dismissLoadingAnim { doRevertLoginButton() } }
                        .doOnResult {
                            /* 两个结果 OK 和 Error
                            * OK：设置 mIsLoginSuccess = true 用于标记
                            * Error：格式化返回的错误信息 并提示
                            * */
                            if (intent.loginResultsOkResp != null) {
                                mIsLoginSuccess = true
                                return@doOnResult
                            }
                            runCatching { intent.loginResultErrorResp!!.mDetail.removePrefix("Error: ") }
                                .onFailure { toast(intent.loginResultErrorResp!!.mDetail, false) }
                                .onSuccess { toast(it, false) }
                        }
                }
            }
        }
    }

    override fun initView() {

        // 设置 内边距属性 实现沉浸式效果
        mBinding.root.setPadding(0, mContext.getStatusBarHeight(), 0, mContext.getNavigationBarHeight())

        // 更新登录按钮的生命周期 （防止内存泄漏！）
        mBinding.userLogin.updateLifecycleObserver(viewLifecycleOwner.lifecycle)
    }

    override fun initListener() {

        // 初始化添加返回事件回调
        mOnBackCallback = object : OnBackPressedCallback(true) { override fun handleOnBackPressed() { findNavController().navigateUp() } }
        requireActivity().onBackPressedDispatcher.addCallback(mOnBackCallback)

        mBinding.userLogin.clickGap { _, _ ->
            // 执行登录 并开启按钮动画
            mUserVM.input(UserIntent.Login(
                getUsername() ?: return@clickGap toast(getString(R.string.user_usr_invalid)),
                getPassword() ?: return@clickGap toast(getString(R.string.user_pwd_invalid))
            ))
            mBinding.userLogin.startAnimation()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mOnBackCallback.remove() // 移除返回事件回调 防止影响其他界面使用返回冲突
    }

    private fun doRevertLoginButton() {
        // 停止动画的同时反转动画 判断标志是否成功 (true : 然后返回上一个界面)
        mBinding.userLogin.stopAnimation()
        mBinding.userLogin.revertAnimation()
        if (mIsLoginSuccess) {
            mBinding.root.showSnackBar(getString(R.string.user_login_success))
            requireActivity().findNavController(com.crow.base.R.id.app_main_fcv).navigateUp()
        }
    }

    // 长度不小于6且不包含空
    private fun getUsername(): String? = mBinding.userLoginEditTextUsr.text.toString().run { if (length < 6 || contains(" ")) return null else this }
    private fun getPassword(): String? = mBinding.userLoginEditTextPwd.text.toString().run { if (length < 6 || contains(" ")) return null else this }

}