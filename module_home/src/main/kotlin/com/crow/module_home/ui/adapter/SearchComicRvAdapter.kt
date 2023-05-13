package com.crow.module_home.ui.adapter

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
import com.crow.module_home.databinding.HomeFragmentSearchRvBinding
import com.crow.module_home.model.resp.search.comic_reuslt.SearchComicResult

class SearchComicRvAdapter(
    inline val doOnTap: (SearchComicResult) -> Unit
) : PagingDataAdapter<SearchComicResult, SearchComicRvAdapter.LoadingViewHolder>(DiffCallback()), IBookAdapterColor<SearchComicRvAdapter.LoadingViewHolder> {

    class DiffCallback: DiffUtil.ItemCallback<SearchComicResult>() {
        override fun areItemsTheSame(oldItem: SearchComicResult, newItem: SearchComicResult): Boolean {
            return oldItem.mPathWord == newItem.mPathWord
        }

        override fun areContentsTheSame(oldItem: SearchComicResult, newItem: SearchComicResult): Boolean {
            return oldItem == newItem
        }
    }

    private var mNameHeight: Int? = null

    inner class LoadingViewHolder(binding: HomeFragmentSearchRvBinding) : BaseGlideLoadingViewHolder<HomeFragmentSearchRvBinding>(binding) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoadingViewHolder {
        return LoadingViewHolder(HomeFragmentSearchRvBinding.inflate(LayoutInflater.from(parent.context), parent,false)).also { vh ->

            val layoutParams = vh.rvBinding.homeSearchRvImage.layoutParams
            layoutParams.width = getComicCardWidth() - mSize10
            layoutParams.height = getComicCardHeight()

            vh.rvBinding.homeSearchRvName.doOnLayout { view ->
                if (mNameHeight == null) mNameHeight = if (vh.rvBinding.homeSearchRvName.lineCount == 1) view.measuredHeight * 2 else view.measuredHeight
                (vh.rvBinding.homeSearchRvName.layoutParams as ConstraintLayout.LayoutParams).height = mNameHeight!!
            }

            vh.rvBinding.homeSearchRvImage.doOnClickInterval {
                doOnTap(getItem(vh.absoluteAdapterPosition) ?: return@doOnClickInterval)
            }
        }
    }

    override fun onBindViewHolder(vh: LoadingViewHolder, position: Int) {
        val item = getItem(position) ?: return

        vh.mLoadingPropertyAnimator?.cancel()
        vh.mTextPropertyAnimator?.cancel()
        vh.mLoadingPropertyAnimator = null
        vh.mTextPropertyAnimator = null
        vh.rvBinding.homeSearchRvLoading.alpha = 1f
        vh.rvBinding.homeSearchRvProgressText.alpha = 1f
        vh.rvBinding.homeSearchRvProgressText.text = AppGlideProgressFactory.PERCENT_0
        vh.mAppGlideProgressFactory?.doRemoveListener()?.doClean()
        vh.mAppGlideProgressFactory = AppGlideProgressFactory.createGlideProgressListener(item.mImageUrl) { _, _, percentage, _, _ ->
            vh.rvBinding.homeSearchRvProgressText.text = AppGlideProgressFactory.getProgressString(percentage)
        }

        Glide.with(vh.itemView.context)
            .load(item.mImageUrl)
            .listener(vh.mAppGlideProgressFactory?.getRequestListener())
            .transition(GenericTransitionOptions<Drawable>().transition { dataSource, _ ->
                if (dataSource == DataSource.REMOTE) {
                    vh.mLoadingPropertyAnimator = vh.rvBinding.homeSearchRvLoading.animateFadeOut()
                    vh.mTextPropertyAnimator = vh.rvBinding.homeSearchRvProgressText.animateFadeOut()
                    DrawableCrossFadeTransition(BASE_ANIM_200L.toInt(), true)
                } else {
                    vh.rvBinding.homeSearchRvLoading.alpha = 0f
                    vh.rvBinding.homeSearchRvProgressText.alpha = 0f
                    NoTransition()
                }
            })
            .into(vh.rvBinding.homeSearchRvImage)

        vh.rvBinding.homeSearchRvName.text = item.mName
        vh.rvBinding.homeSearchRvAuthor.text = item.mAuthor.joinToString { it.mName }
        vh.rvBinding.homeSearchRvHot.text = formatValue(item.mPopular)

        toSetColor(vh, item.mPopular)
    }

    override fun setColor(vh: LoadingViewHolder, color: Int) {
        vh.rvBinding.homeSearchRvName.setTextColor(color)
        vh.rvBinding.homeSearchRvAuthor.setTextColor(color)
        vh.rvBinding.homeSearchRvHot.setTextColor(color)
    }
}