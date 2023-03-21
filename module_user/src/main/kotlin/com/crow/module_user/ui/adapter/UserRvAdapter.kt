package com.crow.module_user.ui.adapter

import android.view.LayoutInflater.from
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.R
import com.crow.base.app.appContext
import com.crow.base.tools.extensions.clickGap
import com.crow.module_user.databinding.UserFragmentRvBinding
import com.crow.module_user.databinding.UserFragmentRvBinding.inflate
import com.crow.module_user.model.resp.user_login.LoginResultsOkResp

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_user/src/main/kotlin/com/crow/module_user/ui/adapter
 * @Time: 2023/3/20 14:41
 * @Author: CrowForKotlin
 * @Description: UserRvAdapter
 * @formatter:on
 **************************/

private typealias Res = @receiver:DrawableRes Int

class UserRvAdapter(
    private val datas: List<Pair<Res, String>>,
    inline val itemTap: (pos: Int, content: String) -> Unit
) : RecyclerView.Adapter<UserRvAdapter.ViewHolder>() {

    inner class ViewHolder(val rvBinding: UserFragmentRvBinding) : RecyclerView.ViewHolder(rvBinding.root)

    private var mUserInfo: LoginResultsOkResp? = null
    private var mTextView: TextView? = null

    override fun getItemCount(): Int = datas.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(inflate(from(parent.context), parent, false)).also { vh ->
        vh.itemView.clickGap { _, _ -> itemTap(vh.absoluteAdapterPosition, datas[vh.absoluteAdapterPosition].second) }
    }
    override fun onBindViewHolder(vh: ViewHolder, position: Int) {
        if (position == 0) {
            vh.rvBinding.userRvImage.doOnLayout {
                it.layoutParams.height = appContext.resources.getDimensionPixelSize(R.dimen.base_dp64)
                it.layoutParams.width = appContext.resources.getDimensionPixelSize(R.dimen.base_dp64)
            }
        }
        val data = datas[position]
        vh.rvBinding.userRvImage.setImageDrawable(ContextCompat.getDrawable(appContext, data.first))
        vh.rvBinding.userRvText.text = data.second
    }

    fun setData(userInfo: LoginResultsOkResp) { mUserInfo = userInfo }
}