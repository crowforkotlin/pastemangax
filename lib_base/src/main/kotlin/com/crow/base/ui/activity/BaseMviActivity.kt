package com.crow.base.ui.activity

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.viewbinding.ViewBinding
import com.crow.base.copymanga.glide.AppGlideProgressFactory
import com.crow.base.tools.extensions.repeatOnLifecycle
import com.crow.base.ui.viewmodel.mvi.BaseMviIntent
import com.crow.base.ui.viewmodel.mvi.BaseMviViewModel
import com.crow.base.ui.viewmodel.mvi.IBaseMvi

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/kotlin/com/crow/base/ui/activity
 * @Time: 2023/4/6 14:31
 * @Author: CrowForKotlin
 * @Description: BaseMviActivity
 * @formatter:on
 **************************/
abstract class BaseMviActivity<out VB: ViewBinding> : BaseActivityImpl(), IBaseMvi {

    protected val mBinding by lazy { getViewBinding() }

    override fun initView(savedInstanceState: Bundle?) {}

    override fun initListener() {}

    /* 子类强制重写下方三个函数 获取ViewModel ViewBinding OnCreate初始化 */
    abstract fun getViewBinding(): VB

    open fun initObserver(savedInstanceState: Bundle?) {}

    override fun <I : BaseMviIntent> BaseMviViewModel<I>.onOutput(state: Lifecycle.State, baseMviSuspendResult: BaseMviViewModel.BaseMviSuspendResult<I>) {
        repeatOnLifecycle(state) { output { intent -> baseMviSuspendResult.onResult(intent) } }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        initObserver(savedInstanceState)
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
    }

    override fun onDestroy() {
        super.onDestroy()
        AppGlideProgressFactory.doReset()
    }
}