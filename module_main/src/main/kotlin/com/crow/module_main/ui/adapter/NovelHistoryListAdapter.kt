package com.crow.module_main.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.crow.mangax.copymanga.appComicCardHeight
import com.crow.mangax.copymanga.appComicCardWidth
import com.crow.mangax.copymanga.formatHotValue
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.mangax.copymanga.entity.AppConfigEntity
import com.crow.mangax.tools.language.ChineseConverter
import com.crow.mangax.ui.adapter.MangaCoilVH
import com.crow.module_main.databinding.MainHistoryRvBinding
import com.crow.module_main.model.resp.novel_history.NovelHistoryResult
import kotlinx.coroutines.launch
import com.crow.module_book.R as bookR

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Package: com.crow.module_main.ui.adapter
 * @Time: 2023/10/3 18:39
 * @Author: CrowForKotlin
 * @Description: HistoryListAdapter
 * @formatter:on
 **************************/
class NovelHistoryListAdapter(
    private val mLifecycleScope: LifecycleCoroutineScope,
    private val onClick: (name: String, pathword: String) -> Unit)
    : PagingDataAdapter<NovelHistoryResult, NovelHistoryListAdapter.HistoryVH>(DiffCallback()) {

    inner class HistoryVH(binding: MainHistoryRvBinding) : MangaCoilVH<MainHistoryRvBinding>(binding) {

        init {
            initComponent(binding.loading, binding.loadingText, binding.image)

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
            val context = itemView.context

            val novel = item.mBook

            if (AppConfigEntity.mChineseConvert) {
                mLifecycleScope.launch {
                    binding.name.text = ChineseConverter.convert(novel.mName)
                    binding.readed.text = ChineseConverter.convert(context.getString(bookR.string.book_readed_chapter, item.mLastChapterName))
                    binding.lastest.text = ChineseConverter.convert(context.getString(bookR.string.book_new_chapter, novel.mLastChapterName))
                }
            } else {
                binding.name.text = novel.mName
                binding.readed.text = context.getString(bookR.string.book_readed_chapter, item.mLastChapterName)
                binding.lastest.text = context.getString(bookR.string.book_new_chapter, novel.mLastChapterName)
            }
            binding.time.text = context.getString(bookR.string.book_update, novel.mDatetimeUpdated)
            binding.author.text = context.getString(bookR.string.book_author, novel.mAuthor.joinToString { it.mName })
            binding.hot.text = context.getString(bookR.string.book_hot, formatHotValue(novel.mPopular))

            loadImage(novel.mCover)
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