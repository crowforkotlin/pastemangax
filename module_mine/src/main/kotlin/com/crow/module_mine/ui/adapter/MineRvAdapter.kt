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

    inner class ViewHolder(val rvBinding: MineFragmentRvBinding) : RecyclerView.ViewHolder(rvBinding.root)

    private var mUserInfo: MineLoginResultsOkResp? = null
    private var mTextView: TextView? = null

    override fun getItemCount(): Int = datas.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(MineFragmentRvBinding.inflate(from(parent.context), parent, false)).also { vh ->
        vh.itemView.doOnClickInterval { itemTap(vh.absoluteAdapterPosition, datas[vh.absoluteAdapterPosition].second) }
    }
    override fun onBindViewHolder(vh: ViewHolder, position: Int) {
        if (position == 0) {
            vh.rvBinding.userRvImage.doOnLayout {
                it.layoutParams.height = app.resources.getDimensionPixelSize(R.dimen.base_dp64)
                it.layoutParams.width = app.resources.getDimensionPixelSize(R.dimen.base_dp64)
            }
        }
        val data = datas[position]
        vh.rvBinding.userRvImage.setImageDrawable(ContextCompat.getDrawable(app, data.first))
        vh.rvBinding.userRvText.text = data.second
    }

    fun setData(userInfo: MineLoginResultsOkResp) { mUserInfo = userInfo }
}