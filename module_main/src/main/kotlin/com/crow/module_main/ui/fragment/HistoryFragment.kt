package com.crow.module_main.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.addCallback
import com.crow.base.copymanga.entity.Fragments
import com.crow.base.tools.extensions.navigateIconClickGap
import com.crow.base.tools.extensions.popSyncWithClear
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.module_main.databinding.MainFragmentHistoryBinding

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Package: com.crow.module_main.ui.fragment
 * @Time: 2023/10/2 19:36
 * @Author: CrowForKotlin
 * @Description: HistoryFragment
 * @formatter:on
 **************************/
class HistoryFragment : BaseMviFragment<MainFragmentHistoryBinding>() {

    override fun getViewBinding(inflater: LayoutInflater) = MainFragmentHistoryBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {

        // 设置 内边距属性 实现沉浸式效果
        immersionRoot()

    }

    override fun initListener() {

        // 返回
        mBinding.topbar.navigateIconClickGap { navigateUp() }
    }

    override fun onStart() {
        super.onStart()
        mBackDispatcher = requireActivity().onBackPressedDispatcher.addCallback(this) { navigateUp() }
    }


    private fun navigateUp() = parentFragmentManager.popSyncWithClear(Fragments.History.name)
}