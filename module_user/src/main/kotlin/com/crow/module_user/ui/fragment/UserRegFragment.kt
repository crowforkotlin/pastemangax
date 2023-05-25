package com.crow.module_user.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.addCallback
import androidx.lifecycle.lifecycleScope
import com.crow.base.app.appContext
import com.crow.base.copymanga.BaseEventEnum
import com.crow.base.copymanga.BaseUser
import com.crow.base.copymanga.entity.Fragments
import com.crow.base.copymanga.updateLifecycleObserver
import com.crow.base.tools.coroutine.FlowBus
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.tools.extensions.getNavigationBarHeight
import com.crow.base.tools.extensions.getStatusBarHeight
import com.crow.base.tools.extensions.popSyncWithClear
import com.crow.base.tools.extensions.showSnackBar
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnLoading
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.base.ui.viewmodel.doOnSuccess
import com.crow.module_user.R
import com.crow.module_user.databinding.UserFragmentRegBinding
import com.crow.module_user.model.UserIntent
import com.crow.module_user.ui.viewmodel.UserViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_user/src/main/kotlin/com/crow/module_user/ui/fragment
 * @Time: 2023/4/8 14:35
 * @Author: CrowForKotlin
 * @Description: UserRegFragment
 * @formatter:on
 **************************/
class UserRegFragment : BaseMviFragment<UserFragmentRegBinding>() {

    // 共享用户VM
    private val mUserVM by sharedViewModel<UserViewModel>()

    // 是否注冊成功？
    private var mIsRegSuccess = false

    // 值是否相同？ true 返回 this false 返回 null
    private fun String?.getIsSame(target: String) : String? = if(this == target) this else null

    // 反转登录按钮
    private fun doRevertRegButton() {

        // 停止动画
        mBinding.userReg.stopAnimation()

        // 反转动画
        mBinding.userReg.revertAnimation()

        // 判断标志是否成功 (true : 然后返回上一个界面)
        if (mIsRegSuccess) {
            FlowBus.with<String>(BaseEventEnum.LoginScuess.name).post(lifecycleScope, getString(R.string.user_reg_ok))
            navigateUp()
        }
    }

    // 返回
    private fun navigateUp() = parentFragmentManager.popSyncWithClear(Fragments.Reg.toString())

    override fun getViewBinding(inflater: LayoutInflater) = UserFragmentRegBinding.inflate(inflater)

    override fun onStart() {
        super.onStart()
        mBackDispatcher = requireActivity().onBackPressedDispatcher.addCallback(this) { navigateUp() }
    }

    override fun initView(bundle: Bundle?) {

        // 设置 内边距属性 实现沉浸式效果
        mBinding.root.setPadding(0, mContext.getStatusBarHeight(), 0, mContext.getNavigationBarHeight())

        // 更新登录按钮的生命周期 （防止内存泄漏！）
        mBinding.userReg.updateLifecycleObserver(viewLifecycleOwner.lifecycle)
    }

    override fun initListener() {

        mBinding.userReg.doOnClickInterval {
            // 执行登录
            mUserVM.input(UserIntent.Reg(
                mUserVM.getUsername(mBinding.userRegEditTextUsr.text.toString()) ?: return@doOnClickInterval toast(getString(
                    R.string.user_usr_invalid)),
                (mUserVM.getPassword(mBinding.userRegEditTextPwd.text.toString()) ?: return@doOnClickInterval toast(getString(R.string.user_pwd_invalid)))
                    .getIsSame(mBinding.userRegEditTextRePwd.text.toString()) ?: return@doOnClickInterval toast(getString(R.string.user_repwd_notmatch)
            )))

            // 开启按钮动画
            mBinding.userReg.startAnimation()
        }
    }

    override fun initObserver() {
        mUserVM.onOutput { intent ->
            when(intent) {
                is UserIntent.Reg -> {
                    intent.mBaseViewState
                        .doOnLoading { showLoadingAnim() }
                        .doOnError { _, msg ->
                            dismissLoadingAnim { doRevertRegButton() }
                            toast(msg ?: appContext.getString(
                                com.crow.base.R.string.BaseUnknowError))
                        }
                        .doOnResult {
                            /* 两个结果 OK 和 Error
                            * OK：设置 mIsRegSuccess = true 用于标记
                            * Error：格式化返回的错误信息 并提示
                            * */
                            if (intent.regResultsOkResp != null) {
                                mIsRegSuccess = true
                                if (BaseUser.CURRENT_USER_TOKEN.isEmpty())
                                    mUserVM.input(UserIntent.Login(
                                        mUserVM.getUsername(mBinding.userRegEditTextUsr.text.toString()) ?: return@doOnResult,
                                        mUserVM.getPassword(mBinding.userRegEditTextPwd.text.toString()) ?: return@doOnResult
                                    ))
                                else dismissLoadingAnim { doRevertRegButton() }
                                return@doOnResult
                            }
                            dismissLoadingAnim { doRevertRegButton() }
                            runCatching { intent.userResultErrorResp!!.mDetail.removePrefix("Error: ") }
                                .onFailure { toast(intent.userResultErrorResp!!.mDetail, false) }
                                .onSuccess { toast(it, false) }
                        }

                }

                is UserIntent.Login -> {
                    intent.mBaseViewState
                        .doOnSuccess { dismissLoadingAnim { doRevertRegButton() } }
                        .doOnError { _, msg -> mBinding.root.showSnackBar(msg ?: appContext.getString(
                            com.crow.base.R.string.BaseUnknowError)) }
                        .doOnResult {
                            /* 两个结果 OK 和 Error
                            * OK：设置 mIsRegSuccess = true 用于标记
                            * Error：格式化返回的错误信息 并提示
                            * */
                            if (intent.loginResultsOkResp != null) return@doOnResult
                            runCatching { intent.userResultErrorResp!!.mDetail.removePrefix("Error: ") }
                                .onFailure { toast(intent.userResultErrorResp!!.mDetail, false) }
                                .onSuccess { toast(it, false) }
                        }
                }
            }
        }
    }
}