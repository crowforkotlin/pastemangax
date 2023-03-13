package com.crow.base.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

abstract class BaseMviFragment<out VB : ViewBinding> : BaseVBFragmentImpl() {

    private var _mBinding: VB? = null
    protected val mBinding get() = _mBinding!!
    protected lateinit var mContext: Context

    abstract fun getViewBinding(inflater: LayoutInflater): VB

    override fun initObserver() {}
    override fun initListener() {}
    override fun initView() {}

    override fun getView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return getViewBinding(inflater).also { _mBinding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mContext = requireContext()
        initObserver()
        super.onViewCreated(view, savedInstanceState)
    }
}