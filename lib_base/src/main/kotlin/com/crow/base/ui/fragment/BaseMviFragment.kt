package com.crow.base.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.viewbinding.ViewBinding
import com.crow.base.tools.extensions.repeatOnLifecycle
import com.crow.base.ui.viewmodel.mvi.BaseMviIntent
import com.crow.base.ui.viewmodel.mvi.BaseMviViewModel

abstract class BaseMviFragment<out VB : ViewBinding> : BaseFragmentImpl() {

    private var _mBinding: VB? = null
    protected val mBinding get() = _mBinding!!
    protected lateinit var mContext: Context

    abstract fun getViewBinding(inflater: LayoutInflater): VB
    override fun initObserver() {}
    override fun initListener() {}
    override fun initView() {}

    fun <I : BaseMviIntent> BaseMviViewModel<I>.onOutput(state: Lifecycle.State = Lifecycle.State.CREATED, baseMviSuspendResult: BaseMviViewModel.BaseMviSuspendResult<I>) {
        repeatOnLifecycle(state) { output { intent -> baseMviSuspendResult.onResult(intent) } }
    }

    override fun getView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return getViewBinding(inflater).also { _mBinding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mContext = requireContext()
        initObserver()
        super.onViewCreated(view, savedInstanceState)
    }
}