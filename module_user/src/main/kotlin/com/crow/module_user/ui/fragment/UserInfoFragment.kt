package com.crow.module_user.ui.fragment

import android.view.LayoutInflater
import com.crow.base.tools.extensions.*
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.module_user.databinding.UserFragmentInfoBinding
import com.crow.module_user.ui.viewmodel.UserViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_user/src/main/kotlin/com/crow/module_user/ui/fragment
 * @Time: 2023/3/20 23:02
 * @Author: CrowForKotlin
 * @Description: UserInfoFragment
 * @formatter:on
 **************************/
class UserInfoFragment : BaseMviFragment<UserFragmentInfoBinding>() {

    private val mUserVM by sharedViewModel<UserViewModel>()

    override fun getViewBinding(inflater: LayoutInflater) = UserFragmentInfoBinding.inflate(inflater)

    override fun initView() {

        // 设置 内边距属性 实现沉浸式效果
        mBinding.root.setPadding(0, mContext.getStatusBarHeight(), 0, mContext.getNavigationBarHeight())
    }

    override fun initListener() {
        // 头像 点击事件
        mBinding.userIcon.clickGap { _, _ ->
            toast("123")
            navigate(com.crow.base.R.id.mainUsericonfragment)
        }
    }

    override fun initObserver() {

        // 用户信息 收集
        mUserVM.userInfo.onCollect(this) {

            // 初始化 Icon链接 设置用户名 退出可见 修改适配器数据
            mUserVM.doLoadIcon(mContext, false) { resource ->  mBinding.userIcon.setImageDrawable(resource) }
        }
    }
}