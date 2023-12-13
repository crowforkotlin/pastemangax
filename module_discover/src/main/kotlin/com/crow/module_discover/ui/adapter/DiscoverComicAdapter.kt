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
import com.crow.mangax.copymanga.entity.IBookAdapterColor
import com.crow.mangax.copymanga.formatValue
import com.crow.mangax.copymanga.glide.AppGlideProgressFactory
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.mangax.ui.adapter.BaseGlideLoadingViewHolder
import com.crow.base.ui.view.ToolTipsView
import com.crow.mangax.tools.language.ChineseConverter
import com.crow.module_discover.databinding.DiscoverFragmentRvBinding
import com.crow.module_discover.model.resp.comic_home.DiscoverComicHomeResult
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class DiscoverComicAdapter(
    inline val mDoOnTapComic: (DiscoverComicHomeResult) -> Unit
) : PagingDataAdapter<DiscoverComicHomeResult, DiscoverComicAdapter.LoadingViewHolder>(DiffCallback()), IBookAdapterColor<DiscoverComicAdapter.LoadingViewHolder> {

    class DiffCallback : DiffUtil.ItemCallback<DiscoverComicHomeResult>() {
        override fun areItemsTheSame(oldItem: DiscoverComicHomeResult, newItem: DiscoverComicHomeResult): Boolean {
            return oldItem.mName == newItem.mName
        }

        override fun areContentsTheSame(oldItem: DiscoverComicHomeResult, newItem: DiscoverComicHomeResult): Boolean {
            return oldItem == newItem
        }
    }

    class LoadingViewHolder(binding: DiscoverFragmentRvBinding) : BaseGlideLoadingViewHolder<DiscoverFragmentRvBinding>(binding)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : LoadingViewHolder {
        return LoadingViewHolder(DiscoverFragmentRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)).also { vh ->

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

    private val mViewScope = MainScope()

    override fun onBindViewHolder(vh: LoadingViewHolder, position: Int) {
        val item = getItem(position) ?: return

        vh.binding.loading.isVisible = true
        vh.binding.loadingText.isVisible = true
        vh.binding.loadingText.text = AppGlideProgressFactory.PERCENT_0
        vh.mAppGlideProgressFactory?.onRemoveListener()?.onCleanCache()

        vh.mAppGlideProgressFactory = AppGlideProgressFactory.createGlideProgressListener(item.mImageUrl) { _, _, percentage, _, _ ->
            vh.binding.loadingText.text = AppGlideProgressFactory.getProgressString(percentage)
        }

        Glide.with(vh.itemView.context)
            .load(item.mImageUrl)
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
        mViewScope.launch {

            vh.binding.name.text = ChineseConverter.convert(item.mName)
        }
        vh.binding.author.text = item.mAuthor.joinToString { it.mName }
        vh.binding.hot.text = formatValue(item.mPopular)
        vh.binding.time.text = item.mDatetimeUpdated
    }

    override fun setColor(vh: LoadingViewHolder, color: Int) {
        vh.binding.name.setTextColor(color)
        vh.binding.author.setTextColor(color)
        vh.binding.hot.setTextColor(color)
        vh.binding.time.setTextColor(color)
    }
}