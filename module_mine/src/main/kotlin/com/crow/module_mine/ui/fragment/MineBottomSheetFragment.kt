package com.crow.module_mine.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.crow.base.app.app
import com.crow.base.tools.coroutine.FlowBus
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.tools.extensions.navigateToWithBackStack
import com.crow.base.tools.extensions.onCollect
import com.crow.base.tools.extensions.showSnackBar
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.fragment.BaseMviBottomSheetDialogFragment
import com.crow.mangax.R.id.app_main_fcv
import com.crow.mangax.copymanga.BaseEventEnum
import com.crow.mangax.copymanga.MangaXAccountConfig
import com.crow.mangax.copymanga.entity.AppConfig
import com.crow.mangax.copymanga.entity.CatlogConfig
import com.crow.mangax.copymanga.entity.Fragments
import com.crow.module_mine.R
import com.crow.module_mine.databinding.MineFragmentBinding
import com.crow.module_mine.ui.adapter.MineRvAdapter
import com.crow.module_mine.ui.viewmodel.MineViewModel
import com.google.android.material.R.id.design_bottom_sheet
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.core.qualifier.named
import com.crow.mangax.R as mangaR

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_user/src/main/kotlin/com/crow/module_user/ui/fragment
 * @Time: 2023/3/18 21:20
 * @Author: CrowForKotlin
 * @Description: UserRepository
 * @formatter:on
 **************************/

class MineBottomSheetFragment : BaseMviBottomSheetDialogFragment<MineFragmentBinding>() {

    /**
     * ⦁ (Activity 级别)用戶 VM
     *
     * ⦁ 2023-07-02 20:31:08 周日 下午
     */
    private val mUserVM by activityViewModel<MineViewModel>()

    // 用户适配器数据
    private val mAdapterData = mutableListOf (
        R.drawable.mine_ic_usr_24dp to app.getString(R.string.mine_login),
        R.drawable.mine_ic_reg_24dp to app.getString(R.string.mine_reg),
        R.drawable.mine_ic_history_24dp to app.getString(R.string.mine_browsing_history),
        mangaR.drawable.base_ic_download_24dp to app.getString(R.string.mine_download),
        R.drawable.mine_ic_about_24dp to app.getString(R.string.mine_about),
        R.drawable.mine_ic_update_24dp to app.getString(R.string.mine_check_update),
        R.drawable.mine_ic_update_history_24dp to app.getString(R.string.mine_update_history_title)
    )

    // 用户适配器
    private lateinit var mMineRvAdapter: MineRvAdapter

    private fun navigate(tag: String) {
        val parentFragment = parentFragmentManager.findFragmentByTag(Fragments.Container.name)!!
        parentFragmentManager.navigateToWithBackStack(
            id = app_main_fcv,
            hideTarget = parentFragment,
            addedTarget = get(named(tag)),
            tag = tag,
            backStackName = tag
        )
    }

    override fun getViewBinding(inflater: LayoutInflater) = MineFragmentBinding.inflate(inflater)


    override fun onStart() {
        super.onStart()

        dialog?.let { dialog ->
            // 配置行为
            (dialog as BottomSheetDialog).apply {
                dismissWithAnimation = true
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.isDraggable = false
                behavior.saveFlags = BottomSheetBehavior.SAVE_ALL
            }

            // 沉浸式
            dialog.window?.let { window ->
                window.statusBarColor = Color.TRANSPARENT
                window.navigationBarColor = Color.TRANSPARENT
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = !CatlogConfig.mDarkMode
                }
            }

            // 设置BottomSheet的 高度
            dialog.findViewById<View>(design_bottom_sheet)?.apply {
                layoutParams!!.height = resources.displayMetrics.heightPixels
                layoutParams!!.width = resources.displayMetrics.widthPixels
            }
        }
    }

    override fun initView(bundle: Bundle?) {

        mMineRvAdapter = MineRvAdapter(mAdapterData) { pos, content ->

            // 根据 位置 做对应的逻辑处理
            dismissAllowingStateLoss()
            when (pos) {
                // 登录 ＆ 个人信息
                0 -> if (content == getString(R.string.mine_info)) navigate(Fragments.MineInfo.name) else navigate(Fragments.Login.name)
                1 -> navigate(Fragments.Reg.name)
                2 -> navigate(Fragments.History.name)
                3 -> toast(getString(mangaR.string.mangax_dev_future))
                4 -> navigate(Fragments.About.name)
                5 -> FlowBus.with<Unit>(BaseEventEnum.UpdateApp.name).post(lifecycleScope, Unit)
                6 -> navigate(Fragments.UpdateHistory.name)
            }
        }

        // 设置 适配器
        mBinding.userRv.adapter = mMineRvAdapter
    }

    override fun initObserver(saveInstanceState: Bundle?) {

        // 用户信息 收集
        mUserVM.userInfo.onCollect(this) {

            // 防止空指针异常 很大概率和内存重启有关
            val binding = runCatching { mBinding }.getOrNull() ?: return@onCollect

            // 初始化 Icon链接 设置用户名 退出可见 修改适配器数据
            mUserVM.doLoadIcon(mContext, false) { resource ->  binding.userIcon.setImageDrawable(resource) }

            // 数据空 则退出
            if (it == null) return@onCollect

            // 设置昵称
            binding.userName.text = getString(R.string.mine_nickname, it.mNickname)

            // 退出按钮可见
            binding.userExit.visibility = View.VISIBLE

            // 移除适配器首位数据 默认是 登录
            mAdapterData.removeFirst()

            // 索引0插入数据
            mAdapterData.add(0, R.drawable.mine_ic_usr_24dp to getString(R.string.mine_info))
        }
    }

    override fun initListener() {

        // 点击 头像事件
        mBinding.userIcon.doOnClickInterval {

            // 点击头像 并 深链接跳转
            dismissAllowingStateLoss()

            // 导航至头像Fragment Token不为空则跳转
            parentFragmentManager.navigateToWithBackStack<MineIconFragment>(
                app_main_fcv, this,
                bundleOf("iconUrl" to if (MangaXAccountConfig.mAccountToken.isNotEmpty()) mUserVM.mIconUrl else null),
                Fragments.Icon.name, Fragments.Icon.name
            )
        }

        // 点击 退出事件
        mBinding.userExit.doOnClickInterval {

            // 发送事件清除用户全部数据
            parentFragmentManager.setFragmentResult(BaseEventEnum.LoginCategories.name, bundleOf("isLogout" to true))

            // SnackBar提示
            mBinding.root.showSnackBar(getString(R.string.mine_exit_sucess))

            // 关闭当前界面
            dismissAllowingStateLoss()
        }
    }
}