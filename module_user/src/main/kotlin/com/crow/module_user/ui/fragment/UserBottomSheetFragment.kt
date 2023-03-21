package com.crow.module_user.ui.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.crow.base.R.drawable.base_ic_download_24dp
import com.crow.base.app.appContext
import com.crow.base.current_project.BaseUser
import com.crow.base.tools.extensions.clickGap
import com.crow.base.tools.extensions.navigate
import com.crow.base.tools.extensions.onCollect
import com.crow.base.ui.fragment.BaseMviBottomSheetDF
import com.crow.module_user.R
import com.crow.module_user.databinding.UserFragmentBinding
import com.crow.module_user.ui.adapter.UserRvAdapter
import com.crow.module_user.ui.viewmodel.UserViewModel
import com.google.android.material.R.id.design_bottom_sheet
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import com.crow.base.R.id as baseId

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
        base_ic_download_24dp to appContext.getString(R.string.user_download),
        R.drawable.user_ic_about_24dp to appContext.getString(R.string.user_about)
    )

    // 用户适配器
    private val mUserRvAdapter: UserRvAdapter by lazy {
        UserRvAdapter(mAdapterData) { pos, content ->
            when (pos) {
                0 -> {
                    dismissAllowingStateLoss()
                    if (content == getString(R.string.user_info))
                        navigate(baseId.mainUserinfofragment)
                    else
                        navigate(baseId.mainUserloginfragment)
                }
            }
        }
    }

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

            if (it == null) return@onCollect

            mBinding.userName.text = getString(R.string.user_nickname, it.mNickname)
            mBinding.userExit.visibility = View.VISIBLE
            mAdapterData.removeFirst()
            mAdapterData.add(0, R.drawable.user_ic_usr_24dp to getString(R.string.user_info))
        }

        mBinding.userIcon.clickGap { _, _ ->

            // 点击头像 并 深链接跳转
            dismissAllowingStateLoss()
            navigate(baseId.mainUsericonfragment, bundleOf("iconUrl" to if (BaseUser.CURRENT_USER_TOKEN.isNotEmpty()) mUserVM.mIconUrl else null))
        }

        mBinding.userExit.clickGap { _, _ ->

            // 退出账号 并 清除用户数据 置空 Token
            BaseUser.CURRENT_USER_TOKEN = ""
            dismissAllowingStateLoss()
            mUserVM.onClearUserInfo()
        }
    }
}