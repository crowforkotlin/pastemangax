package com.crow.module_home.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.mangax.copymanga.appComicCardHeight
import com.crow.mangax.copymanga.appComicCardWidth
import com.crow.mangax.copymanga.appDp10
import com.crow.mangax.copymanga.entity.IBookAdapterColor
import com.crow.mangax.copymanga.formatHotValue
import com.crow.mangax.copymanga.tryConvert
import com.crow.mangax.ui.adapter.MangaCoilVH
import com.crow.module_home.databinding.HomeFragmentSearchRvBinding
import com.crow.module_home.model.resp.search.comic_reuslt.SearchComicResult

class SearchComicRvAdapter(
    val mLifecycleScope: LifecycleCoroutineScope,
    val onClick: (SearchComicResult) -> Unit
) : PagingDataAdapter<SearchComicResult, SearchComicRvAdapter.LoadingViewHolder>(DiffCallback()), IBookAdapterColor<SearchComicRvAdapter.LoadingViewHolder> {

    class DiffCallback: DiffUtil.ItemCallback<SearchComicResult>() {
        override fun areItemsTheSame(oldItem: SearchComicResult, newItem: SearchComicResult): Boolean {
            return oldItem.mPathWord == newItem.mPathWord
        }

        override fun areContentsTheSame(oldItem: SearchComicResult, newItem: SearchComicResult): Boolean {
            return oldItem == newItem
        }
    }

    inner class LoadingViewHolder(binding: HomeFragmentSearchRvBinding) : MangaCoilVH<HomeFragmentSearchRvBinding>(binding) {

        init {
            initComponent(binding.loading, binding.loadingText, binding.image)
            val layoutParams = binding.image.layoutParams
            layoutParams.width = appComicCardWidth - appDp10
            layoutParams.height = appComicCardHeight
            binding.image.doOnClickInterval { onClick(getItem(absoluteAdapterPosition) ?: return@doOnClickInterval) }
        }
        fun onBind(item: SearchComicResult) {
            mLifecycleScope.tryConvert(item.mName, binding.name::setText)
            binding.name.text = item.mName
            binding.author.text = item.mAuthor.joinToString { it.mName }
            binding.hot.text = formatHotValue(item.mPopular)
            loadCoverImage(item.mImageUrl)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoadingViewHolder { return LoadingViewHolder(HomeFragmentSearchRvBinding.inflate(LayoutInflater.from(parent.context), parent,false)) }

    override fun onBindViewHolder(vh: LoadingViewHolder, position: Int) { vh.onBind(getItem(position) as SearchComicResult) }

    override fun setColor(vh: LoadingViewHolder, color: Int) {
        vh.binding.name.setTextColor(color)
        vh.binding.author.setTextColor(color)
        vh.binding.hot.setTextColor(color)
    }
}