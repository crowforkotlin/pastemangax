package com.crow.module_book.ui.adapter.comic.reader

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.annotation.IntRange
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.app.app
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.mangax.copymanga.MangaXAccountConfig
import com.crow.base.tools.extensions.doOnInterval
import com.crow.base.tools.extensions.immersionPadding
import com.crow.mangax.ui.adapter.MangaCoilVH
import com.crow.base.ui.view.event.BaseEvent
import com.crow.module_book.databinding.BookComicRvBinding
import com.crow.module_book.databinding.BookFragmentClassicIntentRvBinding
import com.crow.module_book.model.entity.comic.reader.ReaderPrevNextInfo
import com.crow.module_book.model.resp.comic_page.Content

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Package: com.crow.module_book.ui.adapter
 * @Time: 2023/9/1 1:15
 * @Author: CrowForKotlin
 * @formatter:on
 **************************/
class ComicStandardRvAdapter(val onPrevNext: (ReaderPrevNextInfo) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val Header = 0
        private const val Body = 1
        private const val Footer = 2
    }

    var mChapterName: String? = null
    var mComicName: String? = null

    /**
     * ⦁ Diff 回调
     *
     * ⦁ 2023-09-02 20:06:41 周六 下午
     */
    private val mDiffCallback: DiffUtil.ItemCallback<Any> = object : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return oldItem == newItem
        }
        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return true
        }
    }

    /**
     * ⦁ 异步 Differ 实例
     *
     * ⦁ 2023-09-02 20:06:53 周六 下午
     */
    private val mDiffer = AsyncListDiffer(this, mDiffCallback)


    inner class BodyViewHolder(binding: BookComicRvBinding) : MangaCoilVH<BookComicRvBinding>(binding) {
        init { initComponent(binding.loading, binding.loadingText, binding.image, binding.retry) }

        fun onBind(item: Content) {
            loadImageWithRetry(when {
                item.mImageUrl.contains("c800x.") -> item.mImageUrl.replace("c800x.", "c${MangaXAccountConfig.mResolution}x.")
                item.mImageUrl.contains("c1200x.") -> item.mImageUrl.replace("c1200x.", "c${MangaXAccountConfig.mResolution}x.")
                item.mImageUrl.contains("c1500x.") -> item.mImageUrl.replace("c1500x.", "c${MangaXAccountConfig.mResolution}x.")
                else -> item.mImageUrl
            })
        }

        private fun setLoadingState(hide: Boolean) {
            binding.loading.isInvisible = hide
            binding.loadingText.isInvisible = hide
        }
        private fun setRetryState(hide: Boolean) {
            binding.retry.isGone = hide
        }
    }

    inner class IntentViewHolder(val binding: BookFragmentClassicIntentRvBinding, isNext: Boolean) : RecyclerView.ViewHolder(binding.root) {

        init {
            if (isNext) immersionPadding(binding.root, paddingStatusBar = false, paddingNaviateBar = true)
            else immersionPadding(binding.root, paddingStatusBar = true, paddingNaviateBar = false)

            binding.comicNext.doOnClickInterval() {
                val pos = absoluteAdapterPosition
                if (pos in 0..< itemCount) {
                    onPrevNext(getItem(pos) as ReaderPrevNextInfo)
                }
            }
        }

        fun onBind(item: ReaderPrevNextInfo) {
            binding.comicNext.text = item.mInfo
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            Header ->IntentViewHolder(BookFragmentClassicIntentRvBinding.inflate(LayoutInflater.from(parent.context), parent,false), false)
            Footer ->IntentViewHolder(BookFragmentClassicIntentRvBinding.inflate(LayoutInflater.from(parent.context), parent,false), true)
            Body -> BodyViewHolder(BookComicRvBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> error("Unknown view type!")
        }
    }

    override fun getItemCount(): Int = mDiffer.currentList.size

    override fun getItemViewType(position: Int): Int {

        return when(position) {
            0 -> Header
            itemCount - 1 -> Footer
            else -> Body
        }
    }

    override fun onViewRecycled(vh: RecyclerView.ViewHolder) {
        super.onViewRecycled(vh)
        when(vh) {
            is BodyViewHolder -> { vh.itemView.updateLayoutParams<ViewGroup.LayoutParams> { height = MATCH_PARENT } }
        }
    }

    override fun onBindViewHolder(vh: RecyclerView.ViewHolder, position: Int) {
        when(vh) {
            is BodyViewHolder -> vh.onBind(getItem(position) as Content)
            is IntentViewHolder -> vh.onBind(getItem(position) as ReaderPrevNextInfo)
        }
    }

    private fun getItem(@IntRange(from = 0) position: Int) = mDiffer.currentList[position]

    fun getCurrentList() = mDiffer.currentList

    fun submitList(pages: MutableList<Any>) = mDiffer.submitList(pages)
    fun submitList(pages: MutableList<Any>, callback: Runnable) = mDiffer.submitList(pages, callback)
}