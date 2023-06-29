package com.crow.module_main.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.addCallback
import androidx.lifecycle.lifecycleScope
import com.crow.base.copymanga.BaseEventEnum
import com.crow.base.copymanga.entity.Fragments
import com.crow.base.tools.coroutine.FlowBus
import com.crow.base.tools.extensions.BASE_ANIM_100L
import com.crow.base.tools.extensions.immersionPadding
import com.crow.base.tools.extensions.navigateIconClickGap
import com.crow.base.tools.extensions.popSyncWithClear
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnLoading
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.module_main.databinding.MainFragmentUpdateHistoryBinding
import com.crow.module_main.model.intent.MainIntent
import com.crow.module_main.ui.adapter.UpdateHistoryAdapter
import com.crow.module_main.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Package: com.crow.module_main.ui.fragment
 * @Time: 2023/6/21 0:09
 * @Author: CrowForKotlin
 * @Description: UpdateHistoryFragment
 * @formatter:on
 **************************/
class UpdateHistoryFragment : BaseMviFragment<MainFragmentUpdateHistoryBinding>() {

    /**
     * ● Main ViewModel
     *
     * ● 2023-06-21 00:47:01 周三 上午
     */
    private val mMainVM by viewModel<MainViewModel>()

    /**
     * ● 更新历史记录 适配器
     *
     * ● 2023-06-21 00:51:39 周三 上午
     */
    private val mUpdateAdapter: UpdateHistoryAdapter by lazy { UpdateHistoryAdapter(mutableListOf()) }

    /**
     * ● 返回
     *
     * ● 2023-06-21 00:12:29 周三 上午
     */
    private fun navigateUp() {
        if (arguments?.getBoolean("force_update") == true) FlowBus.with<Unit>(BaseEventEnum.UpdateApp.name).post(viewLifecycleOwner, Unit)
        else parentFragmentManager.popSyncWithClear(Fragments.UpdateHistory.name)
    }

    /**
     * ● 获取ViewBinding
     *
     * ● 2023-06-21 00:12:47 周三 上午
     */
    override fun getViewBinding(inflater: LayoutInflater) =
        MainFragmentUpdateHistoryBinding.inflate(inflater)

    /**
     * ● Lifecycle onStart
     *
     * ● 2023-06-21 00:12:56 周三 上午
     */
    override fun onStart() {
        super.onStart()
        mBackDispatcher = requireActivity().onBackPressedDispatcher.addCallback(this) { navigateUp() }
    }

    /**
     * ● 初始化视图
     *
     * ● 2023-06-21 00:13:06 周三 上午
     */
    override fun initView(savedInstanceState: Bundle?) {
        mBinding.updateAppbar.immersionPadding(hideNaviateBar = false)
        mBinding.updateRv.immersionPadding(hideStatusBar = false)
        mBinding.updateRv.adapter = mUpdateAdapter
    }


    /**
     * ● 初始化数据
     *
     * ● 2023-06-21 00:47:23 周三 上午
     */
    override fun initData(savedInstanceState: Bundle?) {
        mMainVM.input(MainIntent.GetUpdateInfo())
    }

    /**
     * ● 初始化事件
     *
     * ● 2023-06-21 00:38:15 周三 上午
     */
    override fun initListener() {
        mBinding.updateToolbar.navigateIconClickGap { navigateUp() }
    }

    /**
     * ● 初始化观察者
     *
     * ● 2023-06-21 00:49:27 周三 上午
     */
    override fun initObserver(savedInstanceState: Bundle?) {

        mMainVM.onOutput { intent ->
            when (intent) {
                is MainIntent.GetUpdateInfo -> {
                    intent.mBaseViewState
                        .doOnLoading {  }
                        .doOnError { _, _ -> }
                        .doOnResult {
                            if (intent.appUpdateResp == null) return@doOnResult
                            viewLifecycleOwner.lifecycleScope.launch {
                                mUpdateAdapter.doNotify(intent.appUpdateResp, BASE_ANIM_100L / 5)
                            }
                        }
                }
            }
        }
    }
}