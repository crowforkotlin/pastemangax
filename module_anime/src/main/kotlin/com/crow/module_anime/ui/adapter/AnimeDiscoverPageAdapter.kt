package com.crow.module_anime.ui.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.request.transition.DrawableCrossFadeTransition
import com.bumptech.glide.request.transition.NoTransition
import com.crow.mangax.copymanga.appComicCardHeight
import com.crow.mangax.copymanga.entity.IBookAdapterColor
import com.crow.mangax.copymanga.formatHotValue
import com.crow.mangax.copymanga.okhttp.AppProgressFactory
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.mangax.ui.adapter.MangaCoilVH
import com.crow.base.ui.view.TooltipsView
import com.crow.mangax.copymanga.tryConvert
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
            binding.image.layoutParams.height = appComicCardHeight

            binding.card.doOnClickInterval { mDoOnTapComic(getItem(absoluteAdapterPosition) ?: return@doOnClickInterval) }

            binding.root.doOnClickInterval { mDoOnTapComic(getItem(absoluteAdapterPosition) ?: return@doOnClickInterval) }

            TooltipsView.showTipsWhenLongClick(binding.name)
        }
        fun onBind(item: DiscoverPageResult) {
            binding.loading.isVisible = true
            binding.loadingText.isVisible = true
            binding.loadingText.text = AppProgressFactory.PERCENT_0
            mAppProgressFactory?.removeProgressListener()?.remove()

            mAppProgressFactory = AppProgressFactory.createProgressListener(item.mCover) { _, _, percentage, _, _ ->
                binding.loadingText.text = AppProgressFactory.formateProgress(percentage)
            }

            Glide.with(itemView.context)
                .load(item.mCover)
                .addListener(mAppProgressFactory?.getGlideRequestListener())
                .transition(GenericTransitionOptions<Drawable>().transition { dataSource, _ ->
                    if (dataSource == DataSource.REMOTE) {
                        binding.loading.isInvisible = true
                        binding.loadingText.isInvisible = true
                        DrawableCrossFadeTransition(BASE_ANIM_200L.toInt(), true)
                    } else {
                        binding.loading.isInvisible = true
                        binding.loadingText.isInvisible = true
                        NoTransition()
                    }
                })
                .into(binding.image)

            mLifecycleScope.tryConvert(item.mName, binding.name::setText)
            binding.hot.text = formatHotValue(item.mPopular)
            binding.time.text = item.mDatetimeUpdated
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