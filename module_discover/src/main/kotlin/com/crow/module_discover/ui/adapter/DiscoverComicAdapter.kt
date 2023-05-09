package com.crow.module_discover.ui.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.request.transition.DrawableCrossFadeTransition
import com.bumptech.glide.request.transition.NoTransition
import com.crow.base.copymanga.entity.IBookAdapterColor
import com.crow.base.copymanga.formatValue
import com.crow.base.copymanga.getComicCardHeight
import com.crow.base.copymanga.getComicCardWidth
import com.crow.base.copymanga.glide.AppGlideProgressFactory
import com.crow.base.copymanga.mSize10
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.animateFadeOut
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.ui.adapter.BaseGlideViewHolder
import com.crow.base.ui.view.ToolTipsView
import com.crow.module_discover.databinding.DiscoverFragmentRvBinding
import com.crow.module_discover.model.resp.comic_home.DiscoverComicHomeResult

class DiscoverComicAdapter(
    inline val mDoOnTapComic: (DiscoverComicHomeResult) -> Unit
) : PagingDataAdapter<DiscoverComicHomeResult, DiscoverComicAdapter.ViewHolder>(DiffCallback()), IBookAdapterColor<DiscoverComicAdapter.ViewHolder> {

    class DiffCallback : DiffUtil.ItemCallback<DiscoverComicHomeResult>() {
        override fun areItemsTheSame(oldItem: DiscoverComicHomeResult, newItem: DiscoverComicHomeResult): Boolean {
            return oldItem.mName == newItem.mName
        }

        override fun areContentsTheSame(oldItem: DiscoverComicHomeResult, newItem: DiscoverComicHomeResult): Boolean {
            return oldItem == newItem
        }
    }

    class ViewHolder(binding: DiscoverFragmentRvBinding) : BaseGlideViewHolder<DiscoverFragmentRvBinding>(binding)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ViewHolder {
        return ViewHolder(DiscoverFragmentRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)).also {  vh ->

            vh.rvBinding.discoverRvImage.layoutParams.apply {
                width = getComicCardWidth() - mSize10
                height = getComicCardHeight()
            }

            vh.rvBinding.discoverRvBookCard.doOnClickInterval {
                mDoOnTapComic(getItem(vh.absoluteAdapterPosition) ?: return@doOnClickInterval)
            }

            vh.rvBinding.root.doOnClickInterval {
                mDoOnTapComic(getItem(vh.absoluteAdapterPosition) ?: return@doOnClickInterval)
            }

            ToolTipsView.showToolTipsByLongClick(vh.rvBinding.discoverRvName)
        }
    }

    override fun onBindViewHolder(vh: ViewHolder, position: Int) {
        val item = getItem(position) ?: return

        vh.rvBinding.discoverLoading.alpha = 1f
        vh.rvBinding.discoverProgressText.alpha = 1f
        vh.mAppGlideProgressFactory?.doRemoveListener()?.doClean()
        vh.mAppGlideProgressFactory = AppGlideProgressFactory.createGlideProgressListener(item.mImageUrl) { _, _, percentage, _, _ ->
            vh.rvBinding.discoverProgressText.text = AppGlideProgressFactory.getProgressString(percentage)
        }

        Glide.with(vh.itemView.context)
            .load(item.mImageUrl)
            .addListener(vh.mAppGlideProgressFactory?.getRequestListener())
            .transition(GenericTransitionOptions<Drawable>().transition { dataSource, _ ->
                if (dataSource == DataSource.REMOTE) {
                    vh.rvBinding.discoverLoading.animateFadeOut(100)
                    vh.rvBinding.discoverProgressText.animateFadeOut(100)
                    DrawableCrossFadeTransition(BASE_ANIM_200L.toInt(), true)
                } else {
                    vh.rvBinding.discoverLoading.alpha = 0f
                    vh.rvBinding.discoverProgressText.alpha = 0f
                    NoTransition<Drawable>()
                }
            })
            .into(vh.rvBinding.discoverRvImage)

        vh.rvBinding.discoverRvName.text = item.mName
        vh.rvBinding.discoverRvAuthor.text = item.mAuthor.joinToString { it.mName }
        vh.rvBinding.discoverRvHot.text = formatValue(item.mPopular)
        vh.rvBinding.discoverRvTime.text = item.mDatetimeUpdated

        toSetColor(vh, item.mPopular)
    }

    override fun setColor(vh: ViewHolder, color: Int) {
        vh.rvBinding.discoverRvName.setTextColor(color)
        vh.rvBinding.discoverRvAuthor.setTextColor(color)
        vh.rvBinding.discoverRvHot.setTextColor(color)
        vh.rvBinding.discoverRvTime.setTextColor(color)
    }
}