package com.crow.module_discovery

import android.view.LayoutInflater
import com.crow.base.fragment.BaseFragment
import com.crow.module_discovery.databinding.DiscoveryFragmentBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_discovery/src/main/kotlin/com/crow
 * @Time: 2023/3/9 13:23
 * @Author: CrowForKotlin
 * @Description: DiscoveryFragment
 * @formatter:on
 **************************/
class DiscoveryFragment : BaseFragment<DiscoveryFragmentBinding, DiscoveryViewModel>() {

    override fun getViewBinding(inflater: LayoutInflater) = DiscoveryFragmentBinding.inflate(inflater)
    override fun getViewModel(): Lazy<DiscoveryViewModel> = viewModel()


}