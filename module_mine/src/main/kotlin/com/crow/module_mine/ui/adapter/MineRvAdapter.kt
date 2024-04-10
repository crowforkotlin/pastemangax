package com.crow.module_mine.ui.adapter

import android.view.LayoutInflater.from
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.R
import com.crow.base.app.app
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.module_mine.databinding.MineFragmentRvBinding
import com.crow.module_mine.model.resp.MineLoginResultsOkResp

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_user/src/main/kotlin/com/crow/module_user/ui/adapter
 * @Time: 2023/3/20 14:41
 * @Author: CrowForKotlin
 * @Description: UserRvAdapter
 * @formatter:on
 **************************/
class MineRvAdapter(
    private val datas: List<Pair<Int, String>>,
    inline val itemTap: (pos: Int, content: String) -> Unit
) : RecyclerView.Adapter<MineRvAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: MineFragmentRvBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(pair: Pair<Int, String>) {
            binding.userButton.icon = ContextCompat.getDrawable(app, pair.first)
            binding.userButton.text = pair.second
        }
    }

    private var mUserInfo: MineLoginResultsOkResp? = null
    private var mTextView: TextView? = null

    override fun getItemCount(): Int = datas.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(MineFragmentRvBinding.inflate(from(parent.context), parent, false)).also { vh ->
        vh.itemView.doOnClickInterval { itemTap(vh.absoluteAdapterPosition, datas[vh.absoluteAdapterPosition].second) }
    }
    override fun onBindViewHolder(vh: ViewHolder, position: Int) {
        /*if (position == 0) {
            vh.rvBinding.userRvImage.doOnLayout {
                it.layoutParams.height = app.resources.getDimensionPixelSize(R.dimen.base_dp64)
                it.layoutParams.width = app.resources.getDimensionPixelSize(R.dimen.base_dp64)
            }
        }*/
        vh.onBind(datas[position])
    }

    fun setData(userInfo: MineLoginResultsOkResp) { mUserInfo = userInfo }
}