package com.crow.module_home.ui.fragment

import android.view.LayoutInflater
import com.crow.base.fragment.BaseVBFragment
import com.crow.module_home.databinding.HomeFragmentContainerBinding
import com.crow.module_home.ui.HomeViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_home/src/main/kotlin/com/crow/module_home/ui/fragment
 * @Time: 2023/3/7 14:00
 * @Author: BarryAllen
 * @Description: HomeContainerFragment
 * @formatter:on
 **************************/
class HomeContainerFragment : BaseVBFragment<HomeFragmentContainerBinding,HomeViewModel>() {

    override fun getViewBinding(inflater: LayoutInflater) = HomeFragmentContainerBinding.inflate(inflater)
    override fun getViewModel(): Lazy<HomeViewModel> = viewModel()

    override fun initView() {

    }
}