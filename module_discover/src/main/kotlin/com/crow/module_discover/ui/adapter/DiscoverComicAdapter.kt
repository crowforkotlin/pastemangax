package com.crow.module_discover.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.crow.base.current_project.formatValue
import com.crow.base.current_project.getComicCardHeight
import com.crow.base.current_project.getComicCardWidth
import com.crow.base.tools.extensions.clickGap
import com.crow.module_discover.databinding.DiscoverFragmentRvBinding
import com.crow.module_discover.model.resp.home.DiscoverHomeResult
import com.tencent.bugly.proguard.v

class DiscoverComicAdapter(inline val mDoOnTapComic: (DiscoverHomeResult) -> Unit) : PagingDataAdapter<DiscoverHomeResult, DiscoverComicAdapter.ViewHolder>(DiffCallback()) {

    class DiffCallback : DiffUtil.ItemCallback<DiscoverHomeResult>() {
        override fun areItemsTheSame(oldItem: DiscoverHomeResult, newItem: DiscoverHomeResult, ): Boolean {
            return oldItem.mName == newItem.mName
        }

        override fun areContentsTheSame(oldItem: DiscoverHomeResult, newItem: DiscoverHomeResult, ): Boolean {
            return oldItem == newItem
        }
    }

    inner class ViewHolder(val rvBinding: DiscoverFragmentRvBinding) : RecyclerView.ViewHolder(rvBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ViewHolder {
        return ViewHolder(DiscoverFragmentRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)).also {  vh ->

            vh.rvBinding.discoverRvImage.layoutParams.apply {
                width = getComicCardWidth()
                height = getComicCardHeight()
            }

            vh.rvBinding.discoverRvBookCard.clickGap { _, _ ->
                mDoOnTapComic(getItem(vh.absoluteAdapterPosition) ?: return@clickGap)
            }
        }
    }

    override fun onBindViewHolder(vh: ViewHolder, position: Int) {
        val item = getItem(position) ?: return
        Glide.with(vh.itemView.context).load(item.mImageUrl).into(vh.rvBinding.discoverRvImage)
        vh.rvBinding.discoverRvName.text = item.mName
        vh.rvBinding.discoverRvAuthor.text = item.mAuthor.joinToString { it.mName }
        vh.rvBinding.discoverRvHot.text = formatValue(item.mPopular)
        vh.rvBinding.discoverRvTime.text = item.mDatetimeUpdated
    }
}
