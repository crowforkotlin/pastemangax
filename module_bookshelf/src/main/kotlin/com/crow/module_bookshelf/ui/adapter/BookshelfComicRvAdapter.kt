package com.crow.module_bookshelf.ui.adapter

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
import com.crow.base.copymanga.getComicCardHeight
import com.crow.base.copymanga.getComicCardWidth
import com.crow.base.copymanga.glide.AppGlideProgressFactory
import com.crow.base.copymanga.mSize10
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.animateFadeOut
import com.crow.base.tools.extensions.doOnClickInterval

import com.crow.base.ui.adapter.BaseGlideLoadingViewHolder
import com.crow.module_bookshelf.databinding.BookshelfFragmentRvBinding
import com.crow.module_bookshelf.model.resp.bookshelf_comic.BookshelfComicResults

class BookshelfComicRvAdapter(
    inline val doOnTap: (BookshelfComicResults) -> Unit
) : PagingDataAdapter<BookshelfComicResults, BookshelfComicRvAdapter.LoadingViewHolder>(DiffCallback()) {

    class DiffCallback: DiffUtil.ItemCallback<BookshelfComicResults>() {
        override fun areItemsTheSame(oldItem: BookshelfComicResults, newItem: BookshelfComicResults): Boolean {
            return oldItem.mUuid == newItem.mUuid
        }

        override fun areContentsTheSame(oldItem: BookshelfComicResults, newItem: BookshelfComicResults): Boolean {
            return oldItem == newItem
        }
    }

    inner class LoadingViewHolder(binding: BookshelfFragmentRvBinding) : BaseGlideLoadingViewHolder<BookshelfFragmentRvBinding>(binding)

    private var mNameHeight: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoadingViewHolder {
        return LoadingViewHolder(BookshelfFragmentRvBinding.inflate(LayoutInflater.from(parent.context), parent,false)).also { vh ->

            val layoutParams = vh.rvBinding.bookshelfRvImage.layoutParams
            layoutParams.width = getComicCardWidth() - mSize10
            layoutParams.height = getComicCardHeight()

            vh.rvBinding.bookshelfRvName.doOnLayout { view ->
                if (mNameHeight == null) mNameHeight = if (vh.rvBinding.bookshelfRvName.lineCount == 1) view.measuredHeight * 2 else view.measuredHeight
                (vh.rvBinding.bookshelfRvName.layoutParams as ConstraintLayout.LayoutParams).height = mNameHeight!!
            }

            vh.rvBinding.bookshelfRvImage.doOnClickInterval {
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
        vh.rvBinding.bookshelfRvLoading.alpha = 1f
        vh.rvBinding.bookshelfRvProgressText.alpha = 1f
        vh.rvBinding.bookshelfRvProgressText.text = AppGlideProgressFactory.PERCENT_0
        vh.mAppGlideProgressFactory?.doRemoveListener()?.doClean()
        vh.mAppGlideProgressFactory = AppGlideProgressFactory.createGlideProgressListener(item.mComic.mCover) { _, _, percentage, _, _ ->
            vh.rvBinding.bookshelfRvProgressText.text = AppGlideProgressFactory.getProgressString(percentage)
        }

        Glide.with(vh.itemView.context)
            .load(item.mComic.mCover)
            .listener(vh.mAppGlideProgressFactory?.getRequestListener())
            .transition(GenericTransitionOptions<Drawable>().transition { dataSource, _ ->
                if (dataSource == DataSource.REMOTE) {
                    vh.mLoadingPropertyAnimator = vh.rvBinding.bookshelfRvLoading.animateFadeOut()
                    vh.mTextPropertyAnimator = vh.rvBinding.bookshelfRvProgressText.animateFadeOut()
                    DrawableCrossFadeTransition(BASE_ANIM_200L.toInt(), true)
                } else {
                    vh.rvBinding.bookshelfRvLoading.alpha = 0f
                    vh.rvBinding.bookshelfRvProgressText.alpha = 0f
                    NoTransition()
                }
            })
            .into(vh.rvBinding.bookshelfRvImage)
        vh.rvBinding.bookshelfRvName.text = item.mComic.mName
        vh.rvBinding.bookshelfRvTime.text = item.mComic.mDatetimeUpdated
    }
}