package com.crow.module_bookshelf.ui.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.request.transition.DrawableCrossFadeTransition
import com.crow.base.copymanga.getComicCardHeight
import com.crow.base.copymanga.getComicCardWidth
import com.crow.base.copymanga.glide.AppGlideProgressFactory
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.animateFadeOut
import com.crow.base.tools.extensions.doOnClickInterval

import com.crow.base.ui.adapter.BaseGlideViewHolder
import com.crow.module_bookshelf.databinding.BookshelfFragmentRvBinding
import com.crow.module_bookshelf.model.resp.bookshelf_novel.BookshelfNovelResults

class BookshelfNovelRvAdapter(
    inline val doOnTap: (BookshelfNovelResults) -> Unit
) : PagingDataAdapter<BookshelfNovelResults, BookshelfNovelRvAdapter.ViewHolder>(DiffCallback()) {

    class DiffCallback: DiffUtil.ItemCallback<BookshelfNovelResults>() {
        override fun areItemsTheSame(oldItem: BookshelfNovelResults, newItem: BookshelfNovelResults): Boolean {
            return oldItem.mUuid == newItem.mUuid
        }

        override fun areContentsTheSame(oldItem: BookshelfNovelResults, newItem: BookshelfNovelResults): Boolean {
            return oldItem == newItem
        }
    }

    inner class ViewHolder(binding: BookshelfFragmentRvBinding) : BaseGlideViewHolder<BookshelfFragmentRvBinding>(binding)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(BookshelfFragmentRvBinding.inflate(LayoutInflater.from(parent.context), parent,false)).also { vh ->

            vh.rvBinding.bookshelfRvImage.layoutParams.apply {
                width = getComicCardWidth()
                height = getComicCardHeight()
            }

            vh.rvBinding.bookshelfRvImage.doOnClickInterval {
                doOnTap(getItem(vh.absoluteAdapterPosition) ?: return@doOnClickInterval)
            }
        }
    }

    override fun onViewRecycled(vh: ViewHolder) {
        super.onViewRecycled(vh)
        vh.mAppGlideProgressFactory?.doRemoveListener()?.doClean()
        vh.mAppGlideProgressFactory = null
    }


    override fun onBindViewHolder(vh: ViewHolder, position: Int) {
        val item = getItem(position) ?: return


        vh.rvBinding.bookshelfRvLoading.alpha = 1f
        vh.rvBinding.bookshelfRvProgressText.alpha = 1f
        vh.mAppGlideProgressFactory?.doRemoveListener()?.doClean()
        vh.mAppGlideProgressFactory = AppGlideProgressFactory.createGlideProgressListener(item.mNovel.mCover) { _, _, percentage, _, _ ->
            vh.rvBinding.bookshelfRvProgressText.text = AppGlideProgressFactory.getProgressString(percentage)
        }

        Glide.with(vh.itemView.context)
            .load(item.mNovel.mCover)
            .addListener(vh.mAppGlideProgressFactory?.getRequestListener())
            .transition(GenericTransitionOptions<Drawable>().transition { _, _ ->
                vh.rvBinding.bookshelfRvLoading.animateFadeOut()
                vh.rvBinding.bookshelfRvProgressText.animateFadeOut()
                DrawableCrossFadeTransition(BASE_ANIM_200L.toInt(), true)
            })
            .into(vh.rvBinding.bookshelfRvImage)
        vh.rvBinding.bookshelfRvName.text = item.mNovel.mName
        vh.rvBinding.bookshelfRvTime.text = item.mNovel.mDatetimeUpdated
    }
}