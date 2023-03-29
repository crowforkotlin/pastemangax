package com.crow.module_discover.ui.fragment

import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.crow.base.tools.extensions.getNavigationBarHeight
import com.crow.base.tools.extensions.getStatusBarHeight
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.module_discover.R
import com.crow.module_discover.databinding.DiscoverFragmentBinding
import com.crow.module_discover.ui.adapter.DiscoverAdapter
import com.google.android.material.tabs.TabLayoutMediator

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_discovery/src/main/kotlin/com/crow
 * @Time: 2023/3/9 13:23
 * @Author: CrowForKotlin
 * @Description: DiscoveryFragment
 * @formatter:on
 **************************/
class DiscoverFragment : BaseMviFragment<DiscoverFragmentBinding>() {

    private val mFragmentList = mutableListOf<Fragment>(DiscoverComicFragment(), DiscoverNovelFragment())

    private var mDiscoverAdapter: DiscoverAdapter? = null

    override fun getViewBinding(inflater: LayoutInflater) = DiscoverFragmentBinding.inflate(inflater)

    override fun initView() {

        // 设置 内边距属性 实现沉浸式效果
        mBinding.discoverTabLayout.setPadding(0, mContext.getStatusBarHeight(),0, 0)

        // 初始 viewpager2
        mDiscoverAdapter = DiscoverAdapter(mFragmentList, requireActivity().supportFragmentManager, lifecycle)
        mBinding.discoverVp.adapter = mDiscoverAdapter
        mBinding.discoverVp.offscreenPageLimit = 2

        // 关联 tabLayout 和 viewpager2
        TabLayoutMediator(mBinding.discoverTabLayout, mBinding.discoverVp) { tab, pos ->
            when(pos) {
                0 -> {
                    tab.text = getString(R.string.discover_comic)
                }
                1 -> {
                    tab.text = getString(R.string.discover_novel)
                }
            }
        }.attach()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        mDiscoverAdapter = null
    }
}