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
import com.crow.mangax.copymanga.appComicCardHeight
import com.crow.mangax.copymanga.appComicCardWidth
import com.crow.mangax.copymanga.entity.IBookAdapterColor
import com.crow.mangax.copymanga.formatHotValue
import com.crow.mangax.copymanga.okhttp.AppProgressFactory
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.mangax.ui.adapter.BaseGlideLoadingViewHolder
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

    class LoadingViewHolder(binding: AnimeFragmentSearchRvBinding) : BaseGlideLoadingViewHolder<AnimeFragmentSearchRvBinding>(binding)

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
        val item = getItem(position) ?: return

        vh.binding.loading.isVisible = true
        vh.binding.loadingText.isVisible = true
        vh.binding.loadingText.text = AppProgressFactory.PERCENT_0
        vh.mAppGlideProgressFactory?.removeProgressListener()?.remove()

        vh.mAppGlideProgressFactory = AppProgressFactory.createProgressListener(item.mCover) { _, _, percentage, _, _ ->
            vh.binding.loadingText.text = AppProgressFactory.formateProgress(percentage)
        }

        Glide.with(vh.itemView.context)
            .load(item.mCover)
            .addListener(vh.mAppGlideProgressFactory?.getGlideRequestListener())
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
        vh.binding.hot.text = formatHotValue(item.mPopular)
        vh.binding.time.text = item.mYears
    }

    override fun setColor(vh: LoadingViewHolder, color: Int) {
        vh.binding.name.setTextColor(color)
        vh.binding.hot.setTextColor(color)
        vh.binding.time.setTextColor(color)
    }
}