package com.crow.module_user.ui.fragment

import android.view.LayoutInflater
import androidx.core.os.bundleOf
import com.bumptech.glide.Glide
import com.crow.base.tools.extensions.*
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.module_user.databinding.UserFragmentInfoBinding


/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_user/src/main/kotlin/com/crow/module_user/ui/fragment
 * @Time: 2023/3/20 23:02
 * @Author: CrowForKotlin
 * @Description: UserInfoFragment
 * @formatter:on
 **************************/
class UserInfoFragment : BaseMviFragment<UserFragmentInfoBinding>() {

    private var mIconUrl: String? = null

    override fun getViewBinding(inflater: LayoutInflater) = UserFragmentInfoBinding.inflate(inflater)

    override fun initView() {

        mBinding.root.setPadding(0, mContext.getStatusBarHeight(), 0, mContext.getNavigationBarHeight())

        mIconUrl = arguments?.getString("iconUrl") ?: return

        Glide.with(mContext)
            .load(mIconUrl)
            .into(mBinding.userImageview)

        mBinding.userImageview.clickGap { _, _ ->
            toast("123")
            navigate(com.crow.base.R.id.mainUsericonfragment, bundleOf("iconUrl" to mIconUrl))
        }
    }
}