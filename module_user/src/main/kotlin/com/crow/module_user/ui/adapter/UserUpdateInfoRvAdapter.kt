package com.crow.module_user.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.tools.extensions.clickGap
import com.crow.module_user.R
import com.crow.module_user.databinding.UserFragmentInfoRvBinding
import com.crow.module_user.model.resp.LoginResultsOkResp
import kotlinx.coroutines.delay

class UserUpdateInfoRvAdapter(
    private var mDatas: ArrayList<Pair<Res, String>> = arrayListOf(),
    inline val itemTap: (pos: Int, content: String) -> Unit
) : RecyclerView.Adapter<UserUpdateInfoRvAdapter.ViewHolder>() {

    inner class ViewHolder(val rvBinding: UserFragmentInfoRvBinding) : RecyclerView.ViewHolder(rvBinding.root)

    // 用户信息
    private var mUserInfo: LoginResultsOkResp? = null

    override fun getItemCount(): Int = mDatas.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(UserFragmentInfoRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)).also { vh ->
            vh.itemView.clickGap { _, _ -> itemTap(vh.absoluteAdapterPosition, mDatas[vh.absoluteAdapterPosition].second) }
        }
    }

    override fun getItemViewType(position: Int) = position

    override fun onBindViewHolder(vh: ViewHolder, position: Int) {

        // 单个数据源
        val data = mDatas[position]

        // 根据Drawable设置可见性
        when (data.first) {
            R.drawable.user_ic_usr_24dp -> vh.rvBinding.userInfoRvEdit.visibility = View.VISIBLE
            R.drawable.user_ic_gender_24dp -> vh.rvBinding.userInfoRvEdit.visibility = View.VISIBLE
        }

        // 设置名称
        vh.rvBinding.userInfoRvText.text = data.second

        // 文本的StartDrawable
        vh.rvBinding.userInfoRvText.setCompoundDrawablesRelativeWithIntrinsicBounds(ContextCompat.getDrawable(vh.itemView.context, data.first), null, null, null)
    }

    suspend fun doNotify(delay: Long = 50L, waitTime: Long = 100L) {
        repeat(itemCount) {
            notifyItemChanged(it)
            delay(delay)
        }
        delay(waitTime)
    }

    fun setData(datas: ArrayList<Pair<Res, String>>) { mDatas = datas }
}