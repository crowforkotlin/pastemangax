package com.crow.module_discover.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.crow.mangax.copymanga.appComicCardHeight
import com.crow.mangax.copymanga.entity.IBookAdapterColor
import com.crow.mangax.copymanga.formatHotValue
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.mangax.ui.adapter.MangaCoilVH
import com.crow.base.ui.view.TooltipsView
import com.crow.mangax.copymanga.tryConvert
import com.crow.module_discover.databinding.DiscoverFragmentRvBinding
import com.crow.module_discover.model.resp.comic_home.DiscoverComicHomeResult

class DiscoverComicAdapter(
    private val mLifecycleScope: LifecycleCoroutineScope,
    private val onClick: (DiscoverComicHomeResult) -> Unit
) : PagingDataAdapter<DiscoverComicHomeResult, DiscoverComicAdapter.LoadingViewHolder>(DiffCallback()), IBookAdapterColor<DiscoverComicAdapter.LoadingViewHolder> {

    class DiffCallback : DiffUtil.ItemCallback<DiscoverComicHomeResult>() {
        override fun areItemsTheSame(oldItem: DiscoverComicHomeResult, newItem: DiscoverComicHomeResult): Boolean {
            return oldItem.mName == newItem.mName
        }

        override fun areContentsTheSame(oldItem: DiscoverComicHomeResult, newItem: DiscoverComicHomeResult): Boolean {
            return oldItem == newItem
        }
    }

    inner class LoadingViewHolder(binding: DiscoverFragmentRvBinding) : MangaCoilVH<DiscoverFragmentRvBinding>(binding) {

        init {
            initComponent(binding.loading, binding.loadingText, binding.image)

            binding.image.layoutParams.height = appComicCardHeight

            binding.card.doOnClickInterval { onClick(getItem(absoluteAdapterPosition) ?: return@doOnClickInterval) }

            binding.root.doOnClickInterval { onClick(getItem(absoluteAdapterPosition) ?: return@doOnClickInterval) }

            TooltipsView.showTipsWhenLongClick(binding.name)
        }

        fun onBind(item: DiscoverComicHomeResult) {
            loadCoverImage(item.mImageUrl)
            mLifecycleScope.tryConvert(item.mName, binding.name::setText)
            binding.author.text = item.mAuthor.joinToString { it.mName }
            binding.hot.text = formatHotValue(item.mPopular)
            binding.time.text = item.mDatetimeUpdated
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : LoadingViewHolder {
        return LoadingViewHolder(DiscoverFragmentRvBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(vh: LoadingViewHolder, position: Int) {
        vh.onBind(getItem(position) ?: return)
    }

    override fun setColor(vh: LoadingViewHolder, color: Int) {
        vh.binding.name.setTextColor(color)
        vh.binding.author.setTextColor(color)
        vh.binding.hot.setTextColor(color)
        vh.binding.time.setTextColor(color)
    }
}