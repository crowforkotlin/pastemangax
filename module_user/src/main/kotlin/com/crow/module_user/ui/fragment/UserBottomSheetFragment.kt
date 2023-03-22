package com.crow.module_user.ui.fragment

import android.R.anim
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.NavOptions
import com.crow.base.app.appContext
import com.crow.base.current_project.BaseUser
import com.crow.base.tools.extensions.clickGap
import com.crow.base.tools.extensions.navigate
import com.crow.base.tools.extensions.onCollect
import com.crow.base.tools.extensions.showSnackBar
import com.crow.base.ui.fragment.BaseMviBottomSheetDF
import com.crow.module_user.R
import com.crow.module_user.databinding.UserFragmentBinding
import com.crow.module_user.ui.adapter.UserRvAdapter
import com.crow.module_user.ui.viewmodel.UserViewModel
import com.google.android.material.R.id.design_bottom_sheet
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import com.crow.base.R as baseR

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_user/src/main/kotlin/com/crow/module_user/ui/fragment
 * @Time: 2023/3/18 21:20
 * @Author: CrowForKotlin
 * @Description: UserRepository
 * @formatter:on
 **************************/

class UserBottomSheetFragment : BaseMviBottomSheetDF<UserFragmentBinding>() {

    companion object { val TAG = UserBottomSheetFragment::class.java.simpleName }

    // 用户适配器数据
    private val mAdapterData = mutableListOf (
        R.drawable.user_ic_usr_24dp to appContext.getString(R.string.user_login),
        R.drawable.user_ic_reg_24dp to appContext.getString(R.string.user_reg),
        R.drawable.user_ic_history_24dp to appContext.getString(R.string.user_browsing_history),
        baseR.drawable.base_ic_download_24dp to appContext.getString(R.string.user_download),
        R.drawable.user_ic_about_24dp to appContext.getString(R.string.user_about)
    )

    // 用户适配器
    private val mUserRvAdapter: UserRvAdapter by lazy { UserRvAdapter(mAdapterData) { pos, content ->

        // 根据 位置 做对应的逻辑处理
        when (pos) {

            // 登录 ＆ 个人信息
            0 -> {
                dismissAllowingStateLoss()
                if (content == getString(R.string.user_info)) navigate(baseR.id.mainUserinfofragment)
                else navigate(baseR.id.mainUserloginfragment, navOptions = NavOptions.Builder()
                    .setEnterAnim(anim.slide_in_left)
                    .setExitAnim(anim.slide_out_right)
                    .setPopEnterAnim(anim.slide_in_left)
                    .setPopExitAnim(anim.slide_out_right)
                    .build())
            }

            1 -> { }

            2 -> { }

            3 -> { }

            4 -> { }
        }
    } }

    // 用戶 VM
    private val mUserVM by sharedViewModel<UserViewModel>()

    override fun getViewBinding(inflater: LayoutInflater) = UserFragmentBinding.inflate(inflater)

    override fun onStart() {
        super.onStart()

        // 设置BottomSheet的 高度
        dialog?.findViewById<View>(design_bottom_sheet)?.layoutParams!!.height = ViewGroup.LayoutParams.MATCH_PARENT
    }

    override fun initView() {

        // 设置 适配器
        mBinding.userRv.adapter = mUserRvAdapter
    }

    override fun initObserver() {

        // 用户信息 收集
        mUserVM.userInfo.onCollect(this) {

            // 初始化 Icon链接 设置用户名 退出可见 修改适配器数据
            mUserVM.doLoadIcon(mContext, false) { resource ->  mBinding.userIcon.setImageDrawable(resource) }

            // 数据空 则退出
            if (it == null) return@onCollect

            // 设置昵称
            mBinding.userName.text = getString(R.string.user_nickname, it.mNickname)

            // 退出按钮可见
            mBinding.userExit.visibility = View.VISIBLE

            // 移除适配器首位数据 默认是 登录
            mAdapterData.removeFirst()

            // 索引0插入数据
            mAdapterData.add(0, R.drawable.user_ic_usr_24dp to getString(R.string.user_info))
        }

        // 点击 头像事件
        mBinding.userIcon.clickGap { _, _ ->

            // 点击头像 并 深链接跳转
            dismissAllowingStateLoss()

            // 导航至头像Fragment Token不为空则跳转
            navigate(baseR.id.mainUsericonfragment, bundleOf("iconUrl" to if (BaseUser.CURRENT_USER_TOKEN.isNotEmpty()) mUserVM.mIconUrl else null))
        }

        // 点击 退出事件
        mBinding.userExit.clickGap { _, _ ->

            // 清除用户全部数据
            mUserVM.doClearUserInfo()

            // SnackBar提示
            mBinding.root.showSnackBar(getString(R.string.user_exit_sucess))

            // 关闭当前界面
            dismissAllowingStateLoss()
        }
    }
}