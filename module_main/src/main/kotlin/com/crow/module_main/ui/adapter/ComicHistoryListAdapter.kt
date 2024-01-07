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
import com.crow.base.ui.view.TooltipsView
import com.crow.mangax.copymanga.entity.AppConfigEntity.Companion.mChineseConvert
import com.crow.mangax.tools.language.ChineseConverter
import com.crow.mangax.ui.adapter.MangaCoilVH
import com.crow.module_main.databinding.MainHistoryRvBinding
import com.crow.module_main.model.resp.comic_history.ComicHistoryResult
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
class ComicHistoryListAdapter(
    private val mLifecycleScope: LifecycleCoroutineScope,
    private val onClick: (name: String, pathword: String) -> Unit) :
PagingDataAdapter<ComicHistoryResult, ComicHistoryListAdapter.HistoryVH>(DiffCallback()) {

    inner class HistoryVH(binding: MainHistoryRvBinding) : MangaCoilVH<MainHistoryRvBinding>(binding) {

        init {
            initComponent(binding.loading, binding.loadingText, binding.image)
            binding.card.layoutParams.apply {
                width = appComicCardWidth
                height = appComicCardHeight
            }
            itemView.doOnClickInterval {
                val item = (getItem(absoluteAdapterPosition) ?: return@doOnClickInterval).mComic
                onClick(item.mName, item.mPathWord)
            }
            TooltipsView.showTipsWhenLongClick(binding.name)
        }

        fun onBind(item: ComicHistoryResult) {
            val context = itemView.context
            val comic = item.mComic

            if (mChineseConvert) {
                mLifecycleScope.launch {
                    binding.name.text = ChineseConverter.convert(comic.mName)
                    binding.readed.text = ChineseConverter.convert(context.getString(bookR.string.book_readed_chapter, item.mLastChapterName))
                    binding.lastest.text = ChineseConverter.convert(context.getString(bookR.string.book_new_chapter, comic.mLastChapterName))
                }
            } else {
                binding.name.text = comic.mName
                binding.readed.text = context.getString(bookR.string.book_readed_chapter, item.mLastChapterName)
                binding.lastest.text = context.getString(bookR.string.book_new_chapter, comic.mLastChapterName)
            }
            binding.time.text = context.getString(bookR.string.book_update, comic.mDatetimeUpdated)
            binding.author.text = context.getString(bookR.string.book_author, comic.mAuthor.joinToString { it.mName })
            binding.hot.text = context.getString(bookR.string.book_hot, formatHotValue(comic.mPopular))

            loadImage(comic.mCover)
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