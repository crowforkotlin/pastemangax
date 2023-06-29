
package com.crow.module_home.ui.adapter

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
import com.crow.base.copymanga.entity.IBookAdapterColor
import com.crow.base.copymanga.formatValue
import com.crow.base.copymanga.getComicCardHeight
import com.crow.base.copymanga.getComicCardWidth
import com.crow.base.copymanga.glide.AppGlideProgressFactory
import com.crow.base.copymanga.mSize10
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.ui.adapter.BaseGlideLoadingViewHolder
import com.crow.module_home.databinding.HomeFragmentSearchRvBinding
import com.crow.module_home.model.resp.search.novel_result.SearchNovelResult

class SearchNovelRvAdapter(
    inline val doOnTap: (SearchNovelResult) -> Unit
) : PagingDataAdapter<SearchNovelResult, SearchNovelRvAdapter.LoadingViewHolder>(DiffCallback()), IBookAdapterColor<SearchNovelRvAdapter.LoadingViewHolder> {

    class DiffCallback: DiffUtil.ItemCallback<SearchNovelResult>() {
        override fun areItemsTheSame(oldItem: SearchNovelResult, newItem: SearchNovelResult): Boolean {
            return oldItem.mPathWord == newItem.mPathWord
        }

        override fun areContentsTheSame(oldItem: SearchNovelResult, newItem: SearchNovelResult): Boolean {
            return oldItem == newItem
        }
    }

    inner class LoadingViewHolder(binding: HomeFragmentSearchRvBinding) : BaseGlideLoadingViewHolder<HomeFragmentSearchRvBinding>(binding)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoadingViewHolder {
        return LoadingViewHolder(HomeFragmentSearchRvBinding.inflate(LayoutInflater.from(parent.context), parent,false)).also { vh ->

            val layoutParams = vh.rvBinding.homeSearchRvImage.layoutParams
            layoutParams.width = getComicCardWidth() - mSize10
            layoutParams.height = getComicCardHeight()

            vh.rvBinding.homeSearchRvImage.doOnClickInterval {
                doOnTap(getItem(vh.absoluteAdapterPosition) ?: return@doOnClickInterval)
            }
        }
    }

    override fun onBindViewHolder(vh: LoadingViewHolder, position: Int) {
        val item = getItem(position) ?: return

        vh.rvBinding.homeSearchRvLoading.isVisible = true
        vh.rvBinding.homeSearchRvProgressText.isVisible = true
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
                    vh.rvBinding.homeSearchRvLoading.isInvisible = true
                    vh.rvBinding.homeSearchRvProgressText.isInvisible = true
                    DrawableCrossFadeTransition(BASE_ANIM_200L.toInt(), true)
                } else {
                    vh.rvBinding.homeSearchRvLoading.isInvisible = true
                    vh.rvBinding.homeSearchRvProgressText.isInvisible = true
                    NoTransition()
                }
            })
            .into(vh.rvBinding.homeSearchRvImage)
        vh.rvBinding.homeSearchRvName.text = item.mName
        vh.rvBinding.homeSearchRvAuthor.text = item.mAuthor.joinToString { it.mName }
        vh.rvBinding.homeSearchRvHot.text = formatValue(item.mPopular)
    }

    override fun setColor(vh: LoadingViewHolder, color: Int) {
        vh.rvBinding.homeSearchRvName.setTextColor(color)
        vh.rvBinding.homeSearchRvAuthor.setTextColor(color)
        vh.rvBinding.homeSearchRvHot.setTextColor(color)
    }
}