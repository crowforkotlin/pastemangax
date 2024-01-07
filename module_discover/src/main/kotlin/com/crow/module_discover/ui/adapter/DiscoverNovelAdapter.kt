package com.crow.module_discover.ui.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.request.transition.DrawableCrossFadeTransition
import com.bumptech.glide.request.transition.NoTransition
import com.crow.mangax.copymanga.appComicCardHeight
import com.crow.mangax.copymanga.appComicCardWidth
import com.crow.mangax.copymanga.appDp10
import com.crow.mangax.copymanga.entity.IBookAdapterColor
import com.crow.mangax.copymanga.formatHotValue
import com.crow.mangax.copymanga.okhttp.AppProgressFactory
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.mangax.ui.adapter.MangaCoilVH
import com.crow.module_discover.databinding.DiscoverFragmentRvBinding
import com.crow.module_discover.model.resp.novel_home.DiscoverNovelHomeResult

class DiscoverNovelAdapter(
    inline val mDoOnTapComic: (DiscoverNovelHomeResult) -> Unit
) : PagingDataAdapter<DiscoverNovelHomeResult, DiscoverNovelAdapter.ViewHolder>(DiffCallback())
    , IBookAdapterColor<DiscoverNovelAdapter.ViewHolder> {

    class DiffCallback : DiffUtil.ItemCallback<DiscoverNovelHomeResult>() {
        override fun areItemsTheSame(oldItem: DiscoverNovelHomeResult, newItem: DiscoverNovelHomeResult): Boolean {
            return oldItem.mName == newItem.mName
        }

        override fun areContentsTheSame(oldItem: DiscoverNovelHomeResult, newItem: DiscoverNovelHomeResult): Boolean {
            return oldItem == newItem
        }
    }

    inner class ViewHolder(binding: DiscoverFragmentRvBinding) : MangaCoilVH<DiscoverFragmentRvBinding>(binding) {
        init {
            initComponent(binding.loading, binding.loadingText, binding.image)
            val layoutParams = binding.image.layoutParams
            layoutParams.width = appComicCardWidth - appDp10
            layoutParams.height = appComicCardHeight
            binding.card.doOnClickInterval {
                mDoOnTapComic(getItem(absoluteAdapterPosition) ?: return@doOnClickInterval)
            }
        }

        fun onBind(item: DiscoverNovelHomeResult) {
            binding.name.text = item.mName
            binding.author.text = item.mAuthor.joinToString { it.mName }
            binding.hot.text = formatHotValue(item.mPopular)
            binding.time.text = item.mDatetimeUpdated
            loadImage(item.mImageUrl)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ViewHolder { return ViewHolder(DiscoverFragmentRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)) }

    override fun onBindViewHolder(vh: ViewHolder, position: Int) { vh.onBind(getItem(position) ?: return) }

    override fun setColor(vh: ViewHolder, color: Int) {
        vh.binding.name.setTextColor(color)
        vh.binding.author.setTextColor(color)
        vh.binding.time.setTextColor(color)
        vh.binding.hot.setTextColor(color)
    }
}
