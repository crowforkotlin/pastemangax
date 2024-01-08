
package com.crow.module_home.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.mangax.copymanga.appComicCardHeight
import com.crow.mangax.copymanga.appComicCardWidth
import com.crow.mangax.copymanga.appDp10
import com.crow.mangax.copymanga.entity.IBookAdapterColor
import com.crow.mangax.copymanga.formatHotValue
import com.crow.mangax.ui.adapter.MangaCoilVH
import com.crow.module_home.databinding.HomeFragmentSearchRvBinding
import com.crow.module_home.model.resp.search.novel_result.SearchNovelResult

class SearchNovelRvAdapter(
    inline val doOnTap: (SearchNovelResult) -> Unit
) : PagingDataAdapter<SearchNovelResult, SearchNovelRvAdapter.LoadingViewHolder>(DiffCallback()), IBookAdapterColor<SearchNovelRvAdapter.LoadingViewHolder> {

    class DiffCallback: DiffUtil.ItemCallback<SearchNovelResult>() {
        override fun areItemsTheSame(oldItem: SearchNovelResult, newItem: SearchNovelResult): Boolean {
            return oldItem.mPathWord == newItem.mPathWord
        }

        override fun areContentsTheSame(oldItem: SearchNovelResult, newItem: SearchNovelResult): Boolean {
            return oldItem == newItem
        }
    }

    inner class LoadingViewHolder(binding: HomeFragmentSearchRvBinding) : MangaCoilVH<HomeFragmentSearchRvBinding>(binding) {

        init {
            initComponent(binding.loading, binding.loadingText, binding.image)
            val layoutParams = binding.image.layoutParams
            layoutParams.width = appComicCardWidth - appDp10
            layoutParams.height = appComicCardHeight
            binding.image.doOnClickInterval { doOnTap(getItem(absoluteAdapterPosition) ?: return@doOnClickInterval) }
        }

        fun onBind(item: SearchNovelResult) {
            binding.homeSearchRvName.text = item.mName
            binding.homeSearchRvAuthor.text = item.mAuthor.joinToString { it.mName }
            binding.homeSearchRvHot.text = formatHotValue(item.mPopular)
            loadCoverImage(item.mImageUrl)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoadingViewHolder { return LoadingViewHolder(HomeFragmentSearchRvBinding.inflate(LayoutInflater.from(parent.context), parent,false)) }

    override fun onBindViewHolder(vh: LoadingViewHolder, position: Int) { vh.onBind(getItem(position) as SearchNovelResult) }

    override fun setColor(vh: LoadingViewHolder, color: Int) {
        vh.binding.homeSearchRvName.setTextColor(color)
        vh.binding.homeSearchRvAuthor.setTextColor(color)
        vh.binding.homeSearchRvHot.setTextColor(color)
    }
}