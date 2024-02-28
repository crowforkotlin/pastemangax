package com.crow.module_main.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.addCallback
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.crow.mangax.copymanga.BaseEventEnum
import com.crow.mangax.copymanga.entity.Fragments
import com.crow.base.tools.coroutine.FlowBus
import com.crow.base.tools.extensions.BASE_ANIM_100L
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.BASE_ANIM_300L
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.animateFadeOutInVisibility
import com.crow.base.tools.extensions.animateFadeOutGone
import com.crow.base.tools.extensions.immersionPadding
import com.crow.base.tools.extensions.navigateIconClickGap
import com.crow.base.tools.extensions.popSyncWithClear
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnLoading
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.base.ui.viewmodel.doOnSuccess
import com.crow.module_main.databinding.MainFragmentUpdateHistoryBinding
import com.crow.module_main.model.intent.AppIntent
import com.crow.module_main.ui.adapter.UpdateHistoryAdapter
import com.crow.module_main.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
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
        immersionPadding(mBinding.updateAppbar, paddingNaviateBar = false)
        immersionPadding(mBinding.updateRv, paddingStatusBar = false)
    }


    /**
     * ● 初始化数据
     *
     * ● 2023-06-21 00:47:23 周三 上午
     */
    override fun initData(savedInstanceState: Bundle?) {
        lifecycleScope.launch {
            delay(BASE_ANIM_200L shl 1)
            mMainVM.input(AppIntent.GetUpdateInfo())
        }
    }

    /**
     * ● 初始化事件
     *
     * ● 2023-06-21 00:38:15 周三 上午
     */
    override fun initListener() {
        mBinding.updateToolbar.navigateIconClickGap { navigateUp() }

        mBinding.updateRefresh.setOnRefreshListener {
            mMainVM.input(AppIntent.GetUpdateInfo())
        }
    }

    /**
     * ● 初始化观察者
     *
     * ● 2023-06-21 00:49:27 周三 上午
     */
    override fun initObserver(saveInstanceState: Bundle?) {

        mMainVM.onOutput { intent ->
            when (intent) {
                is AppIntent.GetUpdateInfo -> {
                    intent.mViewState
                        .doOnLoading { mBinding.updateRefresh.autoRefreshAnimationOnly() }
                        .doOnError { _, _ ->
                            if (mBinding.updateRv.isVisible) mBinding.updateRv.animateFadeOutInVisibility()
                            mBinding.updateTipsError.animateFadeIn()
                            mBinding.updateRefresh.finishRefresh(BASE_ANIM_300L.toInt() shl 1)
                        }
                        .doOnSuccess { mBinding.updateRefresh.finishRefresh(BASE_ANIM_300L.toInt() shl 1) }
                        .doOnResult {
                            if (intent.appUpdateResp == null) return@doOnResult
                            if (mBinding.updateRefresh.isRefreshing) mBinding.updateRefresh.finishRefresh()
                            if (mBinding.updateRv.isInvisible) mBinding.updateRv.animateFadeIn()
                            if (mBinding.updateTipsError.isVisible) mBinding.updateTipsError.animateFadeOutGone()
                            viewLifecycleOwner.lifecycleScope.launch {
                                mBinding.updateRv.adapter = UpdateHistoryAdapter()
                                (mBinding.updateRv.adapter as UpdateHistoryAdapter).notifyInstet(intent.appUpdateResp.mUpdates, BASE_ANIM_100L)
                            }
                        }
                }
            }
        }
    }
}