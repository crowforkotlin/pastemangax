package com.crow.module_anime.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.crow.mangax.copymanga.appComicCardHeight
import com.crow.mangax.copymanga.appComicCardWidth
import com.crow.mangax.copymanga.entity.IBookAdapterColor
import com.crow.mangax.copymanga.formatHotValue
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.mangax.ui.adapter.MangaCoilVH
import com.crow.base.ui.view.TooltipsView
import com.crow.module_anime.databinding.AnimeFragmentSearchRvBinding
import com.crow.module_anime.model.resp.search.SearchResult

class AnimeSearchPageAdapter(
    inline val mOnClick: (SearchResult) -> Unit
) : PagingDataAdapter<SearchResult, AnimeSearchPageAdapter.LoadingViewHolder>(DiffCallback()), IBookAdapterColor<AnimeSearchPageAdapter.LoadingViewHolder> {

    class DiffCallback : DiffUtil.ItemCallback<SearchResult>() {
        override fun areItemsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean {
            return oldItem.mName == newItem.mName
        }

        override fun areContentsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean {
            return oldItem == newItem
        }
    }

    inner class LoadingViewHolder(binding: AnimeFragmentSearchRvBinding) : MangaCoilVH<AnimeFragmentSearchRvBinding>(binding) {
        init { initComponent(binding.loading, binding.loadingText, binding.image) }

        fun onBind(item: SearchResult) {
            loadCoverImage(item.mCover)
            binding.name.text = item.mName
            binding.hot.text = formatHotValue(item.mPopular)
            binding.time.text = item.mYears
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : LoadingViewHolder {
        return LoadingViewHolder(AnimeFragmentSearchRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)).also { vh ->

            vh.binding.image.layoutParams.apply {
                height = appComicCardHeight
                width = appComicCardWidth
            }

            vh.binding.card.doOnClickInterval {
                mOnClick(getItem(vh.absoluteAdapterPosition) ?: return@doOnClickInterval)
            }

            vh.binding.root.doOnClickInterval {
                mOnClick(getItem(vh.absoluteAdapterPosition) ?: return@doOnClickInterval)
            }

            TooltipsView.showTipsWhenLongClick(vh.binding.name)
        }
    }

    override fun onBindViewHolder(vh: LoadingViewHolder, position: Int) {
        vh.onBind(getItem(position) ?: return)
    }

    override fun setColor(vh: LoadingViewHolder, color: Int) {
        vh.binding.name.setTextColor(color)
        vh.binding.hot.setTextColor(color)
        vh.binding.time.setTextColor(color)
    }
}