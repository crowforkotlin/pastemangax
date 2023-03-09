package com.crow.module_main.fragment

import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.crow.base.extensions.clickGap
import com.crow.base.fragment.BaseVBFragment
import com.crow.module_bookshelf.BookShelfFragment
import com.crow.module_discovery.DiscoveryFragment
import com.crow.module_home.ui.fragment.HomeFragment
import com.crow.module_main.adapter.ContainerAdapter
import com.crow.module_main.databinding.MainFragmentContainerBinding
import com.crow.module_main.ui.StereoPagerTransformer
import com.crow.module_main.viewmodel.ContainerViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.androidx.viewmodel.ext.android.viewModel


/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_home/src/main/kotlin/com/crow/module_home/ui/fragment
 * @Time: 2023/3/7 14:00
 * @Author: CrowForKotlin
 * @Description: HomeContainerFragment
 * @formatter:on
 **************************/
class ContainerFragment : BaseVBFragment<MainFragmentContainerBinding, ContainerViewModel>() {

    private val mContext by lazy { requireContext() }
    private var mContainerAdapter: ContainerAdapter? = null
    private val fragmentList by lazy { mutableListOf<Fragment>(HomeFragment(), DiscoveryFragment(), BookShelfFragment()) }

    override fun getViewBinding(inflater: LayoutInflater) = MainFragmentContainerBinding.inflate(inflater)
    override fun getViewModel(): Lazy<ContainerViewModel> = viewModel()

    override fun initView() {

        mBinding.mainSearchView.setupWithSearchBar(mBinding.mainContainerSearchBar)

        mBinding.mainContainerSearchBar.setOnClickListener {
            mBinding.mainSearchView.show()
        }

        mBinding.mainContaienrToolbar.menu[0].clickGap { _, _ ->
            val dialog = MaterialAlertDialogBuilder(mContext)
            dialog.setTitle("拷贝漫画")
            dialog.setPositiveButton("知道了~", null)
            dialog.show()
        }

        mBinding.mainViewPager.apply {
            mContainerAdapter = ContainerAdapter(fragmentList, childFragmentManager, viewLifecycleOwner.lifecycle)
            adapter = mContainerAdapter
            offscreenPageLimit = 1

            setPageTransformer(StereoPagerTransformer(mContext.resources.displayMetrics.widthPixels.toFloat()))
        }

        TabLayoutMediator(mBinding.mainContainerTabLayout, mBinding.mainViewPager) { tab, pos ->
            when (pos) {
                0 -> {
                    tab.text = getString(com.crow.module_home.R.string.home_homepage)
                    tab.icon = ContextCompat.getDrawable(mContext, com.crow.module_home.R.drawable.ic_home_24dp)
                }
                1 -> {
                    tab.text = getString(com.crow.module_home.R.string.home_recovery)
                    tab.icon = ContextCompat.getDrawable(mContext, com.crow.module_home.R.drawable.ic_discovery_24dp)
                }
                2 -> {
                    tab.text = getString(com.crow.module_home.R.string.home_bookshelf)
                    tab.icon = ContextCompat.getDrawable(mContext, com.crow.module_home.R.drawable.ic_discovery_24dp)
                }
                else -> { }
            }
        }.attach()
    }
}