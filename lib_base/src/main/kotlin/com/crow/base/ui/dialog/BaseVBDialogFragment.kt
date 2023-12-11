package com.crow.base.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.viewmodel.BaseViewModel
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnLoading
import com.crow.base.ui.viewmodel.doOnSuccess

/*************************
 * @Machine: RedmiBook Pro 15
 * @RelativePath: cn\barry\base\dialog\BaseVBDialogFragment.kt
 * @Path: D:\Barry\B_study\Android\Android_Project\JetpackApp\lib_base\src\main\java\cn\barry\base\dialog\BaseVBDialogFragment.kt
 * @Author: CrowForKotlin
 * @Time: 2022/5/2 9:41 周一 上午
 * @Description: 继承此Dialog等待有需要即可进行扩展
 * @formatter:on
 *************************/

abstract class BaseVBDialogFragment<VB : ViewBinding, out VM : BaseViewModel> : BaseVBDialogFragmentImpl() {

    companion object {
        val TAG: String = this::class.java.simpleName
    }

    private var _mBinding: VB? = null
    protected val mBinding get() = _mBinding!!
    protected val mViewModel by lazy { getViewModel().value }

    override fun initView(bundle: Bundle?) {}

    override fun initListener() {}

    override fun initObserver(saveInstanceState: Bundle?) {
        mViewModel.baseViewState.observe(viewLifecycleOwner) { viewState ->
            viewState
                .doOnLoading { showLoadingAnim() }
                .doOnSuccess { dismissLoadingAnim() }
                .doOnError { _, msg ->
                    dismissLoadingAnim()
                    if (msg?.isNotBlank() == true) toast(msg)
                }
        }
    }

    abstract fun getViewBinding(layoutInflater: LayoutInflater): VB
    abstract fun getViewModel(): Lazy<VM>

    override fun getView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return getViewBinding(inflater)
            .also { _mBinding = it }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver(savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _mBinding = null
    }
}
