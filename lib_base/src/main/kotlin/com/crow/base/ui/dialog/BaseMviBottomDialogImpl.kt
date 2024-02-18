package com.crow.base.ui.dialog

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StyleRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.crow.base.tools.extensions.repeatOnLifecycle
import com.crow.base.ui.fragment.IBaseFragment
import com.crow.base.ui.viewmodel.mvi.BaseMviIntent
import com.crow.base.ui.viewmodel.mvi.BaseMviSuspendResult
import com.crow.base.ui.viewmodel.mvi.BaseMviViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/kotlin/com/crow/base/fragment
 * @Time: 2023/3/15 12:34
 * @Author: CrowForKotlin
 * @Description: BaseMviBottomSheetDF
 * @formatter:on
 **************************/
abstract class BaseMviBottomDialog<out VB: ViewBinding> : BottomSheetDialog, IBaseFragment {

    constructor(context: Context) : super(context)
    constructor(context: Context, @StyleRes theme: Int) : super(context, theme)
    constructor(context: Context, cancelable: Boolean, cancelListener: DialogInterface.OnCancelListener) : super(context, cancelable, cancelListener)


    private var _mBinding: VB? = null
    protected val mBinding get() = _mBinding!!

    lateinit var mContext: Context

    abstract fun getViewBinding(view: View): VB

    fun <I : BaseMviIntent> BaseMviViewModel<I>.onOutput(state: Lifecycle.State = Lifecycle.State.CREATED, baseMviSuspendResult: BaseMviSuspendResult<I>) {
        lifecycleScope.launch {
            repeatOnLifecycle(state) { output { intent -> baseMviSuspendResult.onResult(intent) } }
        }
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        _mBinding = null
    }

    override fun setContentView(view: View) {
        super.setContentView(view)
        getViewBinding(view).also { _mBinding = it }.root
    }




    override fun initView(bundle: Bundle?) { }

    override fun initListener() { }

    override fun initObserver(saveInstanceState: Bundle?) { }

}