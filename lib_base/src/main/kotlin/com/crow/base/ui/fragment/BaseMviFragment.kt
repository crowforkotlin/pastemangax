package com.crow.base.ui.fragment

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Lifecycle
import androidx.viewbinding.ViewBinding
import com.crow.base.tools.extensions.repeatOnLifecycle
import com.crow.base.ui.viewmodel.mvi.BaseMviIntent
import com.crow.base.ui.viewmodel.mvi.BaseMviViewModel
import com.crow.base.ui.viewmodel.mvi.IBaseMvi

abstract class BaseMviFragment<out VB : ViewBinding> : BaseFragmentImpl(), IBaseMvi {

    private var _mBinding: VB? = null
    protected val mBinding get() = _mBinding!!
    protected var mBackDispatcher: OnBackPressedCallback? = null
    protected var mHandler: Handler = Handler(Looper.getMainLooper())
    protected lateinit var mContext: Context

    /**
     * 获取ViewBinding
     * @param inflater
     * @return VB
     * */
    abstract fun getViewBinding(inflater: LayoutInflater): VB

    override fun initObserver() {}

    override fun initListener() {}

    override fun initView(bundle: Bundle?) {}

    override fun <I : BaseMviIntent> BaseMviViewModel<I>.onOutput(state: Lifecycle.State, baseMviSuspendResult: BaseMviViewModel.BaseMviSuspendResult<I>) {
        repeatOnLifecycle(state) { output { intent -> baseMviSuspendResult.onResult(intent) } }
    }

    override fun getView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return getViewBinding(inflater).also { _mBinding = it }.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = requireContext()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initObserver()
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _mBinding = null
        mHandler.removeCallbacksAndMessages(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        mBackDispatcher?.remove()
        mBackDispatcher = null
    }
}