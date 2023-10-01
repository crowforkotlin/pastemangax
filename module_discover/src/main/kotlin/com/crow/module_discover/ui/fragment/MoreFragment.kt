package com.crow.module_discover.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import com.crow.base.ui.fragment.BaseMviBottomSheetDialogFragment
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.base.ui.viewmodel.doOnSuccess
import com.crow.module_discover.databinding.DiscoverFragmentComicMoreBinding
import com.crow.module_discover.model.intent.DiscoverIntent
import com.crow.module_discover.ui.viewmodel.DiscoverViewModel
import com.google.android.material.chip.Chip
import org.koin.androidx.viewmodel.ext.android.viewModel

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Package: com.crow.module_discover.ui.fragment
 * @Time: 2023/9/26 22:14
 * @Author: CrowForKotlin
 * @Description: DiscoverComicMoreFragment
 * @formatter:on
 **************************/
class MoreFragment : BaseMviBottomSheetDialogFragment<DiscoverFragmentComicMoreBinding>() {

    /**
     * ● VM
     *
     * ● 2023-09-26 22:19:51 周二 下午
     */
    private val mVM by lazy { requireParentFragment().viewModel<DiscoverViewModel>().value }

    /**
     * ● 获取VB
     *
     * ● 2023-09-26 22:19:06 周二 下午
     */
    override fun getViewBinding(inflater: LayoutInflater) = DiscoverFragmentComicMoreBinding.inflate(layoutInflater)

    override fun initData(savedInstanceState: Bundle?) {

        // 获取标签
        mVM.input(DiscoverIntent.GetComicTag())
    }

    /**
     * ● 初始化视图
     *
     * ● 2023-09-26 22:19:58 周二 下午
     */
    override fun initView(bundle: Bundle?) {
        mVM.onOutput { intent ->
            when(intent) {
                is DiscoverIntent.GetComicTag -> {
                    onTagIntent(intent)
                }
            }
        }
    }

    /**
     * ● 标签意图
     *
     * ● 2023-09-28 22:52:37 周四 下午
     */
    private fun onTagIntent(intent: DiscoverIntent.GetComicTag) {
        intent.mBaseViewState
            .doOnSuccess { }
            .doOnError { _, _ -> }
            .doOnResult {
                intent.comicTagResp?.apply {
                    when(arguments!!.getString("TYPE")) {
                        "CATEGORIES" -> {

                            val chip = Chip(mContext)
                            mBinding.moreChipGroup.addView(chip)
                        }
                        "LOCATION" -> {
                            top.forEach {
                                val chip = Chip(mContext)
                                chip.text = it.mName
                                mBinding.moreChipGroup.addView(chip)
                            }
                        }
                    }
                }
            }
    }
}