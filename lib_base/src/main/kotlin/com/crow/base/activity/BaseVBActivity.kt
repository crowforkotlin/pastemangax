package com.crow.base.activity

import android.os.Bundle
import androidx.viewbinding.ViewBinding
import com.crow.base.extensions.toast
import com.crow.base.viewmodel.BaseViewModel
import com.crow.base.viewmodel.doOnError
import com.crow.base.viewmodel.doOnLoading
import com.crow.base.viewmodel.doOnSuccess

/*************************
 * @ProjectName: JetpackApp
 * @Dir_Path: lib_base/src/main/java/cn/barry/base/activity
 * @Time: 2022/4/26 9:32
 * @Author: CrowForKotlin
 * @Description: BaseViewBindingActivity 父类
 * @formatter:on
 **************************/
abstract class BaseVBActivity<out VB : ViewBinding, out VM : BaseViewModel> : BaseVBActivityImpl() {

    protected val mBinding by lazy { getViewBinding() }
    protected val mViewModel by lazy { getViewModel().value }

    override fun initView() {}
    override fun initListener() {}

    /* 子类强制重写下方三个函数 获取ViewModel ViewBinding OnCreate初始化 */
    abstract fun getViewModel(): Lazy<VM>
    abstract fun getViewBinding(): VB

    open fun initObserver() {
        mViewModel.viewState.observe(this) { viewState ->
            viewState
                .doOnLoading { showLoadingAnim() }
                .doOnSuccess { dismissLoadingAnim() }
                .doOnError { _, msg ->
                    dismissLoadingAnim()
                    if (msg?.isNotBlank() == true) toast(msg)
                }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        initObserver()
    }
}