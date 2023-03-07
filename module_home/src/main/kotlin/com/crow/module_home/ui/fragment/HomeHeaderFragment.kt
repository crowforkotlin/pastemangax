package com.crow.module_home.ui.fragment

import android.view.LayoutInflater
import com.crow.base.fragment.BaseVBFragment
import com.crow.module_home.databinding.HomeFragmentHeaderBinding
import com.crow.module_home.ui.HomeViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeHeaderFragment : BaseVBFragment<HomeFragmentHeaderBinding, HomeViewModel>() {
    override fun getViewBinding(inflater: LayoutInflater) = HomeFragmentHeaderBinding.inflate(inflater)

    override fun getViewModel(): Lazy<HomeViewModel> = viewModel()

    override fun initView() {

    }
}