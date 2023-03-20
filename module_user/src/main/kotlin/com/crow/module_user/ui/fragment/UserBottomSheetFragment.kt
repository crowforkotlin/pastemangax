package com.crow.module_user.ui.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.crow.base.current_project.updateLifecycleObserver
import com.crow.base.tools.extensions.EventGapTime
import com.crow.base.tools.extensions.clickGap
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.fragment.BaseMviBottomSheetDF
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnLoading
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.base.ui.viewmodel.doOnSuccessInCoroutine
import com.crow.module_user.databinding.UserFragmentBinding
import com.crow.module_user.model.UserIntent
import com.crow.module_user.ui.viewmodel.UserViewModel
import kotlinx.coroutines.delay
import org.koin.androidx.viewmodel.ext.android.viewModel

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_user/src/main/kotlin/com/crow/module_user/ui/fragment
 * @Time: 2023/3/18 21:20
 * @Author: CrowForKotlin
 * @Description: UserRepository
 * @formatter:on
 **************************/
class UserBottomSheetFragment : BaseMviBottomSheetDF<UserFragmentBinding>() {

    companion object { val TAG = UserBottomSheetFragment::class.java.simpleName }

    private val mUserVM by viewModel<UserViewModel>()

    override fun getViewBinding(inflater: LayoutInflater) = UserFragmentBinding.inflate(inflater)


    override fun onStart() {
        super.onStart()

        // 设置BottomSheet的 高度
        dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.layoutParams!!.height = ViewGroup.LayoutParams.MATCH_PARENT
    }

    override fun initView() {
        mBinding.userLogin.updateLifecycleObserver(viewLifecycleOwner.lifecycle)
        mBinding.userLogin.clickGap { _, _ ->
            isCancelable = false
            mUserVM.input(UserIntent.Login(
                getUsername() ?: return@clickGap toast("账号长度在6位及以上！"),
                getPassword() ?: return@clickGap toast("密码长度在6位及以上！")
            ))
            mBinding.userLogin.startAnimation()
        }
    }

    override fun initObserver() {
        mUserVM.onOutput { intent ->
            when (intent) {
                is UserIntent.Login -> {
                    intent.mViewState
                        .doOnLoading { }
                        .doOnError { _, msg -> toast(msg ?: "Unknow") }
                        .doOnSuccessInCoroutine { revertLoginBt() }
                        .doOnResult {
                            if (intent.loginResultsOkResp != null) {
                                return@doOnResult
                            }
                            runCatching { intent.loginResultErrorResp!!.mDetail.removePrefix("Error: ") }
                                .onFailure { toast(intent.loginResultErrorResp!!.mDetail) }
                                .onSuccess { toast(it) }
                        }
                }
                else -> { }
            }
        }
    }

    private suspend fun revertLoginBt() {
        delay(EventGapTime.BASE_FLAG_TIME)
        mBinding.userLogin.stopAnimation()
        mBinding.userLogin.revertAnimation()
        isCancelable = true
    }

    private fun getUsername(): String? = mBinding.homeInputEditTextUsr.text.toString().run { if (length < 6 || contains(" ")) return null else this }
    private fun getPassword(): String? = mBinding.homeInputEditTextPwd.text.toString().run { if (length < 6 || contains(" ")) return null else this }
}