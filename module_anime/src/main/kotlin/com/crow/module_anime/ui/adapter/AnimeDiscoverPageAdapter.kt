package com.crow.module_anime.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.ui.view.TooltipsView
import com.crow.mangax.copymanga.appComicCardHeight
import com.crow.mangax.copymanga.entity.IBookAdapterColor
import com.crow.mangax.copymanga.formatHotValue
import com.crow.mangax.copymanga.tryConvert
import com.crow.mangax.ui.adapter.MangaCoilVH
import com.crow.module_anime.databinding.AnimeFragmentRvBinding
import com.crow.module_anime.model.resp.discover.DiscoverPageResult

class AnimeDiscoverPageAdapter(
    val mLifecycleScope: LifecycleCoroutineScope,
    val mDoOnTapComic: (DiscoverPageResult) -> Unit
) : PagingDataAdapter<DiscoverPageResult, AnimeDiscoverPageAdapter.LoadingViewHolder>(DiffCallback()), IBookAdapterColor<AnimeDiscoverPageAdapter.LoadingViewHolder> {

    class DiffCallback : DiffUtil.ItemCallback<DiscoverPageResult>() {
        override fun areItemsTheSame(oldItem: DiscoverPageResult, newItem: DiscoverPageResult): Boolean {
            return oldItem.mName == newItem.mName
        }

        override fun areContentsTheSame(oldItem: DiscoverPageResult, newItem: DiscoverPageResult): Boolean {
            return oldItem == newItem
        }
    }

    inner class LoadingViewHolder(binding: AnimeFragmentRvBinding) : MangaCoilVH<AnimeFragmentRvBinding>(binding) {
        init {
            initComponent(binding.loading, binding.loadingText, binding.image)

            binding.image.layoutParams.height = appComicCardHeight

            binding.card.doOnClickInterval { mDoOnTapComic(getItem(absoluteAdapterPosition) ?: return@doOnClickInterval) }

            binding.root.doOnClickInterval { mDoOnTapComic(getItem(absoluteAdapterPosition) ?: return@doOnClickInterval) }

            TooltipsView.showTipsWhenLongClick(binding.name)
        }
        fun onBind(item: DiscoverPageResult) {
            mLifecycleScope.tryConvert(item.mName, binding.name::setText)
            binding.hot.text = formatHotValue(item.mPopular)
            binding.time.text = item.mDatetimeUpdated
            loadCoverImage(item.mCover)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : LoadingViewHolder {
        return LoadingViewHolder(AnimeFragmentRvBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(vh: LoadingViewHolder, position: Int) { vh.onBind(getItem(position) ?: return) }

    override fun setColor(vh: LoadingViewHolder, color: Int) {
        vh.binding.name.setTextColor(color)
        vh.binding.hot.setTextColor(color)
        vh.binding.time.setTextColor(color)
    }
}