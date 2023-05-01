package com.crow.module_discover.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.crow.base.app.appContext
import com.crow.base.current_project.formatValue
import com.crow.base.current_project.getComicCardHeight
import com.crow.base.current_project.getComicCardWidth
import com.crow.base.current_project.mSize10
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.module_discover.R
import com.crow.module_discover.databinding.DiscoverFragmentRvBinding
import com.crow.module_discover.model.resp.comic_home.DiscoverComicHomeResult

class DiscoverComicAdapter(inline val mDoOnTapComic: (DiscoverComicHomeResult) -> Unit) : PagingDataAdapter<DiscoverComicHomeResult, DiscoverComicAdapter.ViewHolder>(DiffCallback()) {

    class DiffCallback : DiffUtil.ItemCallback<DiscoverComicHomeResult>() {
        override fun areItemsTheSame(oldItem: DiscoverComicHomeResult, newItem: DiscoverComicHomeResult, ): Boolean {
            return oldItem.mName == newItem.mName
        }

        override fun areContentsTheSame(oldItem: DiscoverComicHomeResult, newItem: DiscoverComicHomeResult, ): Boolean {
            return oldItem == newItem
        }
    }

    inner class ViewHolder(val rvBinding: DiscoverFragmentRvBinding) : RecyclerView.ViewHolder(rvBinding.root)

    private val mRed = ContextCompat.getColor(appContext, R.color.discover_red)
    private val mPurple = ContextCompat.getColor(appContext, R.color.discover_purple)
    private val mIndigo = ContextCompat.getColor(appContext, R.color.discover_indigo)
    private val mGreen = ContextCompat.getColor(appContext, R.color.discover_green)
    private val mGrey = ContextCompat.getColor(appContext, R.color.discover_grey)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ViewHolder {
        return ViewHolder(DiscoverFragmentRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)).also {  vh ->

            vh.rvBinding.discoverRvImage.layoutParams.apply {
                width = getComicCardWidth() - mSize10
                height = getComicCardHeight()
            }

            vh.rvBinding.discoverRvBookCard.doOnClickInterval {
                mDoOnTapComic(getItem(vh.absoluteAdapterPosition) ?: return@doOnClickInterval)
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
        when {
            item.mPopular > 1000_0000 -> setColor(vh, mRed)
            item.mPopular > 100_0000 -> setColor(vh, mPurple)
            item.mPopular > 10_0000 -> setColor(vh, mIndigo)
            item.mPopular > 1_0000 -> setColor(vh, mGreen)
            else -> setColor(vh, mGrey)
        }
    }

    private fun setColor(vh: ViewHolder, color: Int) {
        vh.rvBinding.discoverRvName.setTextColor(color)
        vh.rvBinding.discoverRvAuthor.setTextColor(color)
        vh.rvBinding.discoverRvTime.setTextColor(color)
        vh.rvBinding.discoverRvHot.setTextColor(color)
    }
}
