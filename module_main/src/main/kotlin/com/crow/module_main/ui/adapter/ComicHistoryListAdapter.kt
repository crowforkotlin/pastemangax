package com.crow.module_main.ui.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.request.transition.DrawableCrossFadeTransition
import com.bumptech.glide.request.transition.NoTransition
import com.crow.mangax.copymanga.appComicCardHeight
import com.crow.mangax.copymanga.appComicCardWidth
import com.crow.mangax.copymanga.formatHotValue
import com.crow.mangax.copymanga.glide.AppGlideProgressFactory
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.ui.view.TooltipsView
import com.crow.mangax.copymanga.entity.AppConfigEntity.Companion.mChineseConvert
import com.crow.mangax.tools.language.ChineseConverter
import com.crow.mangax.ui.adapter.BaseGlideLoadingViewHolder
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

    inner class HistoryVH(binding: MainHistoryRvBinding) : BaseGlideLoadingViewHolder<MainHistoryRvBinding>(binding) {

        init {
            binding.card.layoutParams.width = appComicCardWidth
            binding.card.layoutParams.height = appComicCardHeight
            itemView.doOnClickInterval {
                val item = (getItem(absoluteAdapterPosition) ?: return@doOnClickInterval).mComic
                onClick(item.mName, item.mPathWord)
            }
            TooltipsView.showTipsWhenLongClick(binding.name)
        }

        fun onBind(item: ComicHistoryResult) {
            binding.loading.isVisible = true
            binding.loadingText.isVisible = true
            binding.loadingText.text = AppGlideProgressFactory.PERCENT_0
            mAppGlideProgressFactory?.onRemoveListener()?.onCleanCache()

            mAppGlideProgressFactory = AppGlideProgressFactory.createGlideProgressListener(item.mComic.mCover) { _, _, percentage, _, _ ->
                binding.loadingText.text = AppGlideProgressFactory.getProgressString(percentage)
            }

            Glide.with(itemView)
                .load(item.mComic.mCover)
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

            if (mChineseConvert) {
                mLifecycleScope.launch {
                    binding.name.text = ChineseConverter.convert(item.mComic.mName)
                    binding.readed.text = ChineseConverter.convert(context.getString(bookR.string.book_readed_chapter, item.mLastChapterName))
                    binding.lastest.text = ChineseConverter.convert(context.getString(bookR.string.BookComicNewChapter, item.mComic.mLastChapterName))
                }
            } else {
                binding.name.text = item.mComic.mName
                binding.readed.text = context.getString(bookR.string.book_readed_chapter, item.mLastChapterName)
                binding.lastest.text = context.getString(bookR.string.BookComicNewChapter, item.mComic.mLastChapterName)
            }
            binding.time.text = context.getString(bookR.string.BookComicUpdate, item.mComic.mDatetimeUpdated)
            binding.author.text = context.getString(bookR.string.BookComicAuthor, item.mComic.mAuthor.joinToString { it.mName })
            binding.hot.text = context.getString(bookR.string.BookComicHot, formatHotValue(item.mComic.mPopular))
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