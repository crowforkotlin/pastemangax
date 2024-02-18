package com.crow.base.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Lifecycle
import androidx.viewbinding.ViewBinding
import com.crow.base.tools.extensions.immersionPadding
import com.crow.base.tools.extensions.log
import com.crow.base.tools.extensions.repeatOnLifecycle
import com.crow.base.tools.extensions.updatePadding
import com.crow.base.ui.viewmodel.mvi.BaseMviIntent
import com.crow.base.ui.viewmodel.mvi.BaseMviSuspendResult
import com.crow.base.ui.viewmodel.mvi.BaseMviViewModel
import com.crow.base.ui.viewmodel.mvi.IBaseMvi

abstract class BaseMviFragment<out VB : ViewBinding> : BaseFragmentImpl(), IBaseMvi {

    private var _mBinding: VB? = null
    protected val mBinding get() = _mBinding!!

    private var _mContext: Context? =  null
    protected val mContext: Context get() = _mContext!!

    protected var mBackDispatcher: OnBackPressedCallback? = null

    abstract fun getViewBinding(inflater: LayoutInflater): VB

    override fun initObserver(saveInstanceState: Bundle?) {}

    override fun initListener() {}

    override fun initView(savedInstanceState: Bundle?) {}

    override fun <I : BaseMviIntent> BaseMviViewModel<I>.onOutput(state: Lifecycle.State, baseMviSuspendResult: BaseMviSuspendResult<I>) {
        repeatOnLifecycle(state) { output { intent -> baseMviSuspendResult.onResult(intent) } }
    }

    override fun getView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = getViewBinding(inflater)
        _mBinding = view
        return view.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _mContext = requireContext()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _mBinding = null
        _mContext = null
        mBackDispatcher?.remove()
        mBackDispatcher = null
    }

    protected fun immersionRoot() {
        // 设置 内边距属性 实现沉浸式效果
        immersionPadding(mBinding.root) { view, insets, _ ->
            view.updatePadding(top = insets.top, bottom = insets.bottom )
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                rightMargin= insets.right
            }
        }
    }
}