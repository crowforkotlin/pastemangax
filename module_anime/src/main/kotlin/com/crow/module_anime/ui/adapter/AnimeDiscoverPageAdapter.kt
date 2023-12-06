package com.crow.module_anime.ui.adapter

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
import com.crow.base.copymanga.entity.IBookAdapterColor
import com.crow.base.copymanga.formatValue
import com.crow.base.copymanga.glide.AppGlideProgressFactory
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.ui.adapter.BaseGlideLoadingViewHolder
import com.crow.base.ui.view.ToolTipsView
import com.crow.module_anime.databinding.AnimeFragmentRvBinding
import com.crow.module_anime.model.resp.discover.DiscoverPageResult

class AnimeDiscoverPageAdapter(
    inline val mDoOnTapComic: (DiscoverPageResult) -> Unit
) : PagingDataAdapter<DiscoverPageResult, AnimeDiscoverPageAdapter.LoadingViewHolder>(DiffCallback()), IBookAdapterColor<AnimeDiscoverPageAdapter.LoadingViewHolder> {

    class DiffCallback : DiffUtil.ItemCallback<DiscoverPageResult>() {
        override fun areItemsTheSame(oldItem: DiscoverPageResult, newItem: DiscoverPageResult): Boolean {
            return oldItem.mName == newItem.mName
        }

        override fun areContentsTheSame(oldItem: DiscoverPageResult, newItem: DiscoverPageResult): Boolean {
            return oldItem == newItem
        }
    }

    class LoadingViewHolder(binding: AnimeFragmentRvBinding) : BaseGlideLoadingViewHolder<AnimeFragmentRvBinding>(binding)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : LoadingViewHolder {
        return LoadingViewHolder(AnimeFragmentRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)).also { vh ->

            vh.binding.image.layoutParams.height = appComicCardHeight

            vh.binding.card.doOnClickInterval {
                mDoOnTapComic(getItem(vh.absoluteAdapterPosition) ?: return@doOnClickInterval)
            }

            vh.binding.root.doOnClickInterval {
                mDoOnTapComic(getItem(vh.absoluteAdapterPosition) ?: return@doOnClickInterval)
            }

            ToolTipsView.showToolTipsByLongClick(vh.binding.name)
        }
    }

    override fun onBindViewHolder(vh: LoadingViewHolder, position: Int) {
        val item = getItem(position) ?: return

        vh.binding.loading.isVisible = true
        vh.binding.loadingText.isVisible = true
        vh.binding.loadingText.text = AppGlideProgressFactory.PERCENT_0
        vh.mAppGlideProgressFactory?.onRemoveListener()?.onCleanCache()

        vh.mAppGlideProgressFactory = AppGlideProgressFactory.createGlideProgressListener(item.mCover) { _, _, percentage, _, _ ->
            vh.binding.loadingText.text = AppGlideProgressFactory.getProgressString(percentage)
        }

        Glide.with(vh.itemView.context)
            .load(item.mCover)
            .addListener(vh.mAppGlideProgressFactory?.getRequestListener())
            .transition(GenericTransitionOptions<Drawable>().transition { dataSource, _ ->
                if (dataSource == DataSource.REMOTE) {
                    vh.binding.loading.isInvisible = true
                    vh.binding.loadingText.isInvisible = true
                    DrawableCrossFadeTransition(BASE_ANIM_200L.toInt(), true)
                } else {
                    vh.binding.loading.isInvisible = true
                    vh.binding.loadingText.isInvisible = true
                    NoTransition()
                }
            })
            .into(vh.binding.image)

        vh.binding.name.text = item.mName
        vh.binding.hot.text = formatValue(item.mPopular)
        vh.binding.time.text = item.mDatetimeUpdated
    }

    override fun setColor(vh: LoadingViewHolder, color: Int) {
        vh.binding.name.setTextColor(color)
        vh.binding.hot.setTextColor(color)
        vh.binding.time.setTextColor(color)
    }
}