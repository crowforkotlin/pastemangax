package com.crow.module_discover.ui.fragment

import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import com.crow.base.current_project.BaseStrings
import com.crow.base.tools.coroutine.FlowBus
import com.crow.base.tools.extensions.getStatusBarHeight
import com.crow.base.tools.extensions.logMsg
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.module_discover.R
import com.crow.module_discover.databinding.DiscoverFragmentBinding
import com.crow.module_discover.ui.adapter.DiscoverAdapter
import com.crow.module_discover.ui.viewmodel.DiscoverViewModel
import com.google.android.material.tabs.TabLayoutMediator
import com.orhanobut.logger.Logger
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_discovery/src/main/kotlin/com/crow
 * @Time: 2023/3/9 13:23
 * @Author: CrowForKotlin
 * @Description: DiscoveryFragment
 * @formatter:on
 **************************/
class DiscoverFragment : BaseMviFragment<DiscoverFragmentBinding>() {
    init {
        FlowBus.with<Int>(BaseStrings.Key.POST_CURRENT_ITEM).register(this) { mDiscoverVM.mCurrentItem = it }
    }

    private val mFragmentList = mutableListOf<Fragment>(DiscoverComicFragment(), DiscoverNovelFragment())

    private var mDiscoverAdapter: DiscoverAdapter? = null

    private val mDiscoverVM by sharedViewModel<DiscoverViewModel>()

    override fun getViewBinding(inflater: LayoutInflater) = DiscoverFragmentBinding.inflate(inflater)

    override fun initView() {

        // 设置 内边距属性 实现沉浸式效果
        mBinding.discoverTabLayout.setPadding(0, mContext.getStatusBarHeight(),0, 0)

        // 初始 viewpager2
        "(Discover Fragment) InitView Start".logMsg(Logger.WARN)
        mDiscoverAdapter = DiscoverAdapter(mFragmentList, childFragmentManager, viewLifecycleOwner.lifecycle)
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
        "(Discover Fragment) InitView End".logMsg(Logger.WARN)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mDiscoverAdapter = null
    }
}