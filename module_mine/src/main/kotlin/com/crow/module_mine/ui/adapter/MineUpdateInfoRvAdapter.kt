package com.crow.module_mine.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.module_mine.R
import com.crow.module_mine.databinding.MineFragmentInfoRvBinding
import com.crow.module_mine.model.resp.MineLoginResultsOkResp
import kotlinx.coroutines.delay

class MineUpdateInfoRvAdapter(
    private var mDatas: ArrayList<Pair<Int, String>> = arrayListOf(),
    inline val itemTap: (pos: Int, content: String) -> Unit
) : RecyclerView.Adapter<MineUpdateInfoRvAdapter.ViewHolder>() {

    inner class ViewHolder(val rvBinding: MineFragmentInfoRvBinding) : RecyclerView.ViewHolder(rvBinding.root)

    // 用户信息
    private var mUserInfo: MineLoginResultsOkResp? = null

    override fun getItemCount(): Int = mDatas.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(MineFragmentInfoRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)).also { vh ->
            vh.itemView.doOnClickInterval { itemTap(vh.absoluteAdapterPosition, mDatas[vh.absoluteAdapterPosition].second) }
        }
    }

    override fun getItemViewType(position: Int) = position

    override fun onBindViewHolder(vh: ViewHolder, position: Int) {

        // 单个数据源
        val data = mDatas[position]

        // 根据Drawable设置可见性
        when (data.first) {
            R.drawable.mine_ic_usr_24dp -> vh.rvBinding.userInfoRvEdit.visibility = View.VISIBLE
            R.drawable.mine_ic_gender_24dp -> vh.rvBinding.userInfoRvEdit.visibility = View.VISIBLE
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

    fun setData(datas: ArrayList<Pair<Int, String>>) { mDatas = datas }
}