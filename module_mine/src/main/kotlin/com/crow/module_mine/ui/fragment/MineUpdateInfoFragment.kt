package com.crow.module_mine.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.crow.base.copymanga.BaseEventEnum
import com.crow.base.copymanga.entity.Fragments
import com.crow.base.copymanga.processTokenError
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.tools.extensions.navigateToWithBackStack
import com.crow.base.tools.extensions.onCollect
import com.crow.base.tools.extensions.popSyncWithClear
import com.crow.base.tools.extensions.showSnackBar
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnLoading
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.module_mine.R
import com.crow.module_mine.databinding.MineFragmentInfoBinding
import com.crow.module_mine.model.MineIntent
import com.crow.module_mine.ui.adapter.MineUpdateInfoRvAdapter
import com.crow.module_mine.ui.viewmodel.MineInfoViewModel
import com.crow.module_mine.ui.viewmodel.MineViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.crow.base.R as baseR


/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_user/src/main/kotlin/com/crow/module_user/ui/fragment
 * @Time: 2023/3/20 23:02
 * @Author: CrowForKotlin
 * @Description: UserInfoFragment
 * @formatter:on
 **************************/
class MineUpdateInfoFragment : BaseMviFragment<MineFragmentInfoBinding>() {

    // 共享用户VM
    private val mUserVM by sharedViewModel<MineViewModel>()

    // 用户 更新信息 VM
    private val mUserUpdateInfoVM by viewModel<MineInfoViewModel>()

    // 用户更新信息适配器
    private var mMineUpdateInfoRvAdapter: MineUpdateInfoRvAdapter? = null

    // 手动退出标志位
    private var mExitFragment = false

    private fun navigateUp() {
        parentFragmentManager.popSyncWithClear(Fragments.MineInfo.name)
        mUserUpdateInfoVM.doClearUserUpdateInfoData()
    }

    override fun onStart() {
        super.onStart()
        mBackDispatcher = requireActivity().onBackPressedDispatcher.addCallback(this) { navigateUp() }
    }

    override fun getViewBinding(inflater: LayoutInflater) = MineFragmentInfoBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {

        // 设置 内边距属性 实现沉浸式效果
        immersionRoot()

    }

    override fun initListener() {

        // 头像 点击事件
        mBinding.userUpdateInfoIcon.doOnClickInterval {
            parentFragmentManager.navigateToWithBackStack<MineIconFragment>(baseR.id.app_main_fcv, this, null, Fragments.Icon.name, Fragments.Icon.name)
        }

        // 退出账号 点击事件
        mBinding.userUpdateInfoExitButton.doOnClickInterval { doExitFragment() }
    }

    override fun initObserver(savedInstanceState: Bundle?) {

        // 恢复 标志位
        mExitFragment = false

        // 用户信息 收集 因为是粘性直接收集即可初始化
        mUserVM.userInfo.onCollect(this) {

            // 判断是否退出Fragment
            if (mExitFragment) return@onCollect

            // 初始化 Icon链接 设置用户名 退出可见 修改适配器数据
            mUserVM.doLoadIcon(mContext, false) { resource -> mBinding.userUpdateInfoIcon.setImageDrawable(resource) }

            // 第一次UserInfo初始化NULL是会观察到的，需做判断设置完头像后退出
            if (it == null) return@onCollect

            // 初始化Rv
            mMineUpdateInfoRvAdapter = MineUpdateInfoRvAdapter(mUserUpdateInfoVM.mUserUpdateInfoData) { _, _ -> }

            // 获取VM的数据 为空则发请求
            if (mUserUpdateInfoVM.mUserUpdateInfoData.isEmpty()) mUserVM.input(MineIntent.GetMineUpdateInfo())

            // itemCount == 0 则设置数据
            if (mMineUpdateInfoRvAdapter!!.itemCount == 0) mUserUpdateInfoVM.setData(it)

            // 设置InfoRv适配器
            mBinding.userUpdateInfoRv.adapter = mMineUpdateInfoRvAdapter
        }

        mUserVM.onOutput { intent ->
            if (intent is MineIntent.GetMineUpdateInfo) {
                intent.mViewState
                    .doOnLoading { showLoadingAnim() }
                    .doOnError { code, msg ->
                        dismissLoadingAnim {
                            // 处理 Token错误的信息
                            mBinding.root.processTokenError(code, msg,
                                doOnCancel =  { doExitFragment() },
                                doOnConfirm = { doExitFragment(true) })
                        }
                    }
                    .doOnResult {
                        dismissLoadingAnim {
                            if (intent.mineUpdateInfoResp == null) {
                                toast(getString(baseR.string.BaseUnknowError))
                                return@dismissLoadingAnim
                            }

                            // 设置 InfoVM的数据
                            mUserUpdateInfoVM.setData(intent.mineUpdateInfoResp.mInfo)

                            // 更新适配器
                            viewLifecycleOwner.lifecycleScope.launch { mMineUpdateInfoRvAdapter?.doNotify() }
                        }
                    }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mMineUpdateInfoRvAdapter = null
    }

    private fun doExitFragment(isNeedNavigateLogin: Boolean = false) {
        viewLifecycleOwner.lifecycleScope.launch {

            // 退出Fragment 标志位 true
            mExitFragment = true

            // 发送事件清除用户全部数据
            parentFragmentManager.setFragmentResult(BaseEventEnum.LoginCategories.name, bundleOf("isLogout" to true))

            // 清除当前界面的用户数据
            mUserUpdateInfoVM.doClearUserUpdateInfoData()

            // SnackBar提示
            mBinding.root.showSnackBar(getString(R.string.mine_exit_sucess))

            // 返回上一个界面
            navigateUp()
        }
    }
}