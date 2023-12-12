package com.crow.module_mine.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.addCallback
import com.crow.base.app.app
import com.crow.mangax.copymanga.BaseEventEnum
import com.crow.mangax.copymanga.BaseStrings
import com.crow.mangax.copymanga.entity.Fragments
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
import com.crow.module_mine.R
import com.crow.module_mine.databinding.MineFragmentLoginBinding
import com.crow.module_mine.model.MineIntent
import com.crow.module_mine.ui.tools.updateLifecycleObserver
import com.crow.module_mine.ui.viewmodel.MineViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import com.crow.base.R as baseR


class MineLoginFragment() : BaseMviFragment<MineFragmentLoginBinding>(){

    constructor(iUserLoginSuccessCallback: IUserLoginSuccessCallback) : this() { mLoginSuccessCallback = iUserLoginSuccessCallback }

    /** 登录成功回调接口 */
    fun interface IUserLoginSuccessCallback {
        fun onLoginSuccess()
    }

    /** （Activity 级别） 用户VM */
    private val mUserVM by activityViewModel<MineViewModel>()

    /** 是否登录成功 */
    private var mIsLoginSuccess: Boolean = false

    /** 登录成功回调 */
    private var mLoginSuccessCallback: IUserLoginSuccessCallback? = null

    /** 返回 */
    private fun navigateUp() = parentFragmentManager.popSyncWithClear(Fragments.Login.name)

    /** 反转登录按钮 */
    private fun doRevertLoginButton() {

        // 停止动画
        mBinding.userLogin.stopAnimation()

        // 反转动画
        mBinding.userLogin.revertAnimation()

        // 判断标志是否成功 (true : 然后返回上一个界面)
        if (mIsLoginSuccess) {
            toast(getString(R.string.mine_login_ok))
            navigateUp()
            parentFragmentManager.setFragmentResult(BaseEventEnum.LoginCategories.name, (arguments ?: Bundle()).also { it.putBoolean(BaseStrings.ENABLE_DELAY, true) })
        }
    }

    /** 获取ViewBinding */
    override fun getViewBinding(inflater: LayoutInflater) = MineFragmentLoginBinding.inflate(inflater)

    /** Lifecycle Start */
    override fun onStart() {
        super.onStart()
        mBackDispatcher = requireActivity().onBackPressedDispatcher.addCallback(this) { navigateUp() }
    }

    /** 初始化观察者 */
    override fun initObserver(saveInstanceState: Bundle?) {
        mUserVM.onOutput { intent ->
            when (intent) {

                // loading(加载动画) -> error(失败) 或 result(成功) -> success(取消动画)
                is MineIntent.Login -> {
                    intent.mViewState
                        .doOnLoading { showLoadingAnim() }
                        .doOnSuccess { dismissLoadingAnim { doRevertLoginButton() } }
                        .doOnError { _, msg -> mBinding.root.showSnackBar(msg ?: app.getString(baseR.string.BaseUnknowError)) }
                        .doOnResult {
                            /* 两个结果 OK 和 Error
                            * OK：设置 mIsLoginSuccess = true 用于标记
                            * Error：格式化返回的错误信息 并提示
                            * */
                            if (intent.mineLoginResultsOkResp != null) {
                                mIsLoginSuccess = true
                                return@doOnResult
                            }
                            runCatching { intent.mineResultErrorResp!!.mDetail.removePrefix("Error: ") }
                                .onFailure { toast(intent.mineResultErrorResp!!.mDetail, false) }
                                .onSuccess { toast(it, false) }
                        }
                }

                else -> { }
            }
        }
    }

    /** 初始化视图 */
    override fun initView(savedInstanceState: Bundle?) {

        // 设置 内边距属性 实现沉浸式效果
        mBinding.root.setPadding(0, mContext.getStatusBarHeight(), 0, mContext.getNavigationBarHeight())

        // 更新登录按钮的生命周期 （防止内存泄漏！）
        mBinding.userLogin.updateLifecycleObserver(viewLifecycleOwner.lifecycle)
    }

    /** 初始化监听器 */
    override fun initListener() {

        mBinding.userLogin.doOnClickInterval {
            // 执行登录
            mUserVM.input(MineIntent.Login(
                mUserVM.getUsername(mBinding.userLoginEditTextUsr.text.toString()) ?: return@doOnClickInterval toast(getString(
                    R.string.mine_usr_invalid)),
                mUserVM.getPassword(mBinding.userLoginEditTextPwd.text.toString()) ?: return@doOnClickInterval toast(getString(
                    R.string.mine_pwd_invalid))
            ))

            // 开启按钮动画
            mBinding.userLogin.startAnimation()
        }
    }
}