package com.crow.module_discover.ui.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnLayout
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
import com.crow.base.ui.adapter.BaseGlideLoadingViewHolder
import com.crow.base.ui.view.ToolTipsView
import com.crow.module_discover.databinding.DiscoverFragmentRvBinding
import com.crow.module_discover.model.resp.comic_home.DiscoverComicHomeResult

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

    private var mNameHeight: Int? =null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : LoadingViewHolder {
        return LoadingViewHolder(DiscoverFragmentRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)).also { vh ->

            val layoutParams = vh.rvBinding.discoverRvImage.layoutParams
            layoutParams.width = getComicCardWidth() - mSize10
            layoutParams.height = getComicCardHeight()

            vh.rvBinding.discoverRvName.doOnLayout { view ->
                if (mNameHeight == null) mNameHeight = if (vh.rvBinding.discoverRvName.lineCount == 1) view.measuredHeight * 2 else view.measuredHeight
                (vh.rvBinding.discoverRvName.layoutParams as ConstraintLayout.LayoutParams).height = mNameHeight!!
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

    override fun onBindViewHolder(vh: LoadingViewHolder, position: Int) {
        val item = getItem(position) ?: return

        vh.mLoadingPropertyAnimator?.cancel()
        vh.mTextPropertyAnimator?.cancel()
        vh.mLoadingPropertyAnimator = null
        vh.mTextPropertyAnimator = null
        vh.rvBinding.discoverLoading.alpha = 1f
        vh.rvBinding.discoverProgressText.alpha = 1f
        vh.rvBinding.discoverProgressText.text = AppGlideProgressFactory.PERCENT_0
        vh.mAppGlideProgressFactory?.doRemoveListener()?.doClean()

        vh.mAppGlideProgressFactory = AppGlideProgressFactory.createGlideProgressListener(item.mImageUrl) { _, _, percentage, _, _ ->
            vh.rvBinding.discoverProgressText.text = AppGlideProgressFactory.getProgressString(percentage)
        }

        Glide.with(vh.itemView.context)
            .load(item.mImageUrl)
            .addListener(vh.mAppGlideProgressFactory?.getRequestListener())
            .transition(GenericTransitionOptions<Drawable>().transition { dataSource, _ ->
                if (dataSource == DataSource.REMOTE) {
                    vh.mLoadingPropertyAnimator = vh.rvBinding.discoverLoading.animateFadeOut(100)
                    vh.mTextPropertyAnimator = vh.rvBinding.discoverProgressText.animateFadeOut(100)
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

    override fun setColor(vh: LoadingViewHolder, color: Int) {
        vh.rvBinding.discoverRvName.setTextColor(color)
        vh.rvBinding.discoverRvAuthor.setTextColor(color)
        vh.rvBinding.discoverRvHot.setTextColor(color)
        vh.rvBinding.discoverRvTime.setTextColor(color)
    }
}