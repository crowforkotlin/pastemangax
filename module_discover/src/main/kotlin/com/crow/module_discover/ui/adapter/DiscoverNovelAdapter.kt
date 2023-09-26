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
import com.crow.base.copymanga.appComicCardHeight
import com.crow.base.copymanga.appComicCardWidth
import com.crow.base.copymanga.appDp10
import com.crow.base.copymanga.entity.IBookAdapterColor
import com.crow.base.copymanga.formatValue
import com.crow.base.copymanga.glide.AppGlideProgressFactory
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.ui.adapter.BaseGlideLoadingViewHolder
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

    inner class ViewHolder(binding: DiscoverFragmentRvBinding) : BaseGlideLoadingViewHolder<DiscoverFragmentRvBinding>(binding)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ViewHolder {
        return ViewHolder(DiscoverFragmentRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)).also { vh ->

            val layoutParams = vh.binding.discoverRvImage.layoutParams
            layoutParams.width = appComicCardWidth - appDp10
            layoutParams.height = appComicCardHeight

            vh.binding.discoverRvBookCard.doOnClickInterval {
                mDoOnTapComic(getItem(vh.absoluteAdapterPosition) ?: return@doOnClickInterval)
            }
        }
    }



    override fun onBindViewHolder(vh: ViewHolder, position: Int) {
        val item = getItem(position) ?: return

        vh.binding.discoverLoading.isVisible = true
        vh.binding.discoverProgressText.isVisible = true
        vh.binding.discoverProgressText.text = AppGlideProgressFactory.PERCENT_0
        vh.mAppGlideProgressFactory?.doRemoveListener()?.doClean()
        vh.mAppGlideProgressFactory = AppGlideProgressFactory.createGlideProgressListener(item.mImageUrl) { _, _, percentage, _, _ ->
            vh.binding.discoverProgressText.text = AppGlideProgressFactory.getProgressString(percentage)
        }
        
        Glide.with(vh.itemView.context)
            .load(item.mImageUrl)
            .listener(vh.mAppGlideProgressFactory?.getRequestListener())
            .transition(GenericTransitionOptions<Drawable>().transition { dataSource, _ ->
                if (dataSource == DataSource.REMOTE) {
                    vh.binding.discoverLoading.isInvisible = true
                    vh.binding.discoverProgressText.isInvisible = true
                    DrawableCrossFadeTransition(BASE_ANIM_200L.toInt(), true)
                } else {
                    vh.binding.discoverLoading.isInvisible = true
                    vh.binding.discoverProgressText.isInvisible = true
                    NoTransition<Drawable>()
                }
            })
            .into(vh.binding.discoverRvImage)

        vh.binding.discoverRvName.text = item.mName
        vh.binding.discoverRvAuthor.text = item.mAuthor.joinToString { it.mName }
        vh.binding.discoverRvHot.text = formatValue(item.mPopular)
        vh.binding.discoverRvTime.text = item.mDatetimeUpdated
    }

    override fun setColor(vh: ViewHolder, color: Int) {
        vh.binding.discoverRvName.setTextColor(color)
        vh.binding.discoverRvAuthor.setTextColor(color)
        vh.binding.discoverRvTime.setTextColor(color)
        vh.binding.discoverRvHot.setTextColor(color)
    }
}
