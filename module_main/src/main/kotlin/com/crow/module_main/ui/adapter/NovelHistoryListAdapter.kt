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
import com.crow.module_main.model.resp.novel_history.NovelHistoryResult
import com.crow.module_book.R as bookR

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Package: com.crow.module_main.ui.adapter
 * @Time: 2023/10/3 18:39
 * @Author: CrowForKotlin
 * @Description: HistoryListAdapter
 * @formatter:on
 **************************/
class NovelHistoryListAdapter(private val onClick: (name: String, pathword: String) -> Unit)
    : PagingDataAdapter<NovelHistoryResult, NovelHistoryListAdapter.HistoryVH>(DiffCallback()) {

    inner class HistoryVH(binding: MainHistoryRvBinding) : BaseGlideLoadingViewHolder<MainHistoryRvBinding>(binding) {

        init {
            binding.card.layoutParams.apply { 
                width = appComicCardWidth
                height = appComicCardHeight
            }
            itemView.doOnClickInterval {
                val item = (getItem(absoluteAdapterPosition) ?: return@doOnClickInterval).mBook
                onClick(item.mName, item.mPathWord)
            }
        }

        fun onBind(item: NovelHistoryResult) {
            binding.loading.isVisible = true
            binding.loadingText.isVisible = true
            binding.loadingText.text = AppGlideProgressFactory.PERCENT_0
            mAppGlideProgressFactory?.onRemoveListener()?.onCleanCache()

            mAppGlideProgressFactory = AppGlideProgressFactory.createGlideProgressListener(item.mBook.mCover) { _, _, percentage, _, _ ->
                binding.loadingText.text = AppGlideProgressFactory.getProgressString(percentage)
            }

            Glide.with(itemView)
                .load(item.mBook.mCover)
                .addListener(mAppGlideProgressFactory?.getRequestListener())
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

            val context = itemView.context

            binding.name.text = item.mBook.mName
            binding.time.text = context.getString(bookR.string.BookComicUpdate, item.mBook.mDatetimeUpdated)
            binding.author.text = context.getString(bookR.string.BookComicAuthor, item.mBook.mAuthor.joinToString { it.mName })
            binding.readed.text = context.getString(bookR.string.book_readed_chapter, item.mLastChapterName)
            binding.lastest.text = context.getString(bookR.string.BookComicNewChapter, item.mBook.mLastChapterName)
            binding.hot.text = context.getString(bookR.string.BookComicHot, formatValue(item.mBook.mPopular))
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<NovelHistoryResult>() {
        override fun areItemsTheSame(oldItem: NovelHistoryResult, newItem: NovelHistoryResult): Boolean {
            return oldItem.mBook == newItem.mBook
        }

        override fun areContentsTheSame(oldItem: NovelHistoryResult, newItem: NovelHistoryResult): Boolean {
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