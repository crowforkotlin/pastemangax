package com.crow.module_main.ui.adapter

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
import com.crow.base.copymanga.appComicCardWidth
import com.crow.base.copymanga.formatValue
import com.crow.base.copymanga.glide.AppGlideProgressFactory
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.ui.adapter.BaseGlideLoadingViewHolder
import com.crow.module_main.databinding.MainHistoryRvBinding
import com.crow.module_main.model.resp.comic_history.ComicHistoryResult
import com.crow.module_book.R as bookR

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Package: com.crow.module_main.ui.adapter
 * @Time: 2023/10/3 18:39
 * @Author: CrowForKotlin
 * @Description: HistoryListAdapter
 * @formatter:on
 **************************/
class ComicHistoryListAdapter(private val onClick: (name: String, pathword: String) -> Unit) :
    PagingDataAdapter<ComicHistoryResult, ComicHistoryListAdapter.HistoryVH>(DiffCallback()) {

    inner class HistoryVH(binding: MainHistoryRvBinding) : BaseGlideLoadingViewHolder<MainHistoryRvBinding>(binding) {

        init {
            binding.discoverRvBookCard.layoutParams.width = appComicCardWidth
            binding.discoverRvBookCard.layoutParams.height = appComicCardHeight
            itemView.doOnClickInterval {
                val item = (getItem(absoluteAdapterPosition) ?: return@doOnClickInterval).mComic
                onClick(item.mName, item.mPathWord)
            }
        }

        fun onBind(item: ComicHistoryResult) {
            binding.discoverLoading.isVisible = true
            binding.discoverProgressText.isVisible = true
            binding.discoverProgressText.text = AppGlideProgressFactory.PERCENT_0
            mAppGlideProgressFactory?.onRemoveListener()?.onCleanCache()

            mAppGlideProgressFactory = AppGlideProgressFactory.createGlideProgressListener(item.mComic.mCover) { _, _, percentage, _, _ ->
                binding.discoverProgressText.text = AppGlideProgressFactory.getProgressString(percentage)
            }

            Glide.with(itemView)
                .load(item.mComic.mCover)
                .addListener(mAppGlideProgressFactory?.getRequestListener())
                .transition(GenericTransitionOptions<Drawable>().transition { dataSource, _ ->
                    if (dataSource == DataSource.REMOTE) {
                        binding.discoverLoading.isInvisible = true
                        binding.discoverProgressText.isInvisible = true
                        DrawableCrossFadeTransition(BASE_ANIM_200L.toInt(), true)
                    } else {
                        binding.discoverLoading.isInvisible = true
                        binding.discoverProgressText.isInvisible = true
                        NoTransition()
                    }
                })
                .into(binding.discoverRvImage)

            val context = itemView.context

            binding.discoverRvName.text = item.mComic.mName
            binding.discoverRvTime.text = context.getString(bookR.string.BookComicUpdate, item.mComic.mDatetimeUpdated)
            binding.discoverRvAuthor.text = context.getString(bookR.string.BookComicAuthor, item.mComic.mAuthor.joinToString { it.mName })
            binding.discoverRvReaded.text = context.getString(bookR.string.book_readed_chapter, item.mLastChapterName)
            binding.discoverRvLastest.text = context.getString(bookR.string.BookComicNewChapter, item.mComic.mLastChapterName)
            binding.discoverRvHot.text = context.getString(bookR.string.BookComicHot, formatValue(item.mComic.mPopular))
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ComicHistoryResult>() {
        override fun areItemsTheSame(oldItem: ComicHistoryResult, newItem: ComicHistoryResult): Boolean {
            return oldItem.mComic == newItem.mComic
        }

        override fun areContentsTheSame(oldItem: ComicHistoryResult, newItem: ComicHistoryResult): Boolean {
            return oldItem == newItem
        }
    }

    override fun onBindViewHolder(holder: HistoryVH, position: Int) {
        holder.onBind(getItem(position) ?: return)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryVH {
        return HistoryVH(MainHistoryRvBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }
}