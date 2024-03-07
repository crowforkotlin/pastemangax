
package com.crow.module_book.ui.adapter.comic.reader.layoutmanager

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import androidx.annotation.IntRange
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.animateFadeOutGone
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.mangax.copymanga.MangaXAccountConfig
import com.crow.mangax.ui.adapter.MangaCoilVH
import com.crow.module_book.databinding.BookComicLoadingRvBinding
import com.crow.module_book.databinding.BookComicPageRvBinding
import com.crow.module_book.model.entity.comic.reader.ReaderInfo
import com.crow.module_book.model.entity.comic.reader.ReaderLoading
import com.crow.module_book.model.resp.comic_page.Content


/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_comic/src/main/kotlin/com/crow/module_comic/ui/adapter
 * @Time: 2023/3/15 16:42
 * @Author: CrowForKotlin
 * @Description: ComicInfoChapterRvAdapter
 * @formatter:on
 **************************/
class ComicPageRvAdapter(val onRetry: (uuid: String, isNext: Boolean) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val LOADING_VIEW = 0
        private const val CONTENT_VIEW = 1
    }

    private val mDiffCallback: DiffUtil.ItemCallback<Any> = object : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return if (oldItem is ReaderLoading && newItem is ReaderLoading) {
                if (newItem.mMessage == null && oldItem.mMessage == null) {
                    oldItem.mStateComplete == newItem.mStateComplete
                } else {
                   oldItem.mMessage == newItem.mMessage
                   false
                }
            } else if(oldItem is Content && newItem is Content) {
                oldItem.mImageUrl == newItem.mImageUrl
            } else false
        }

        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return if (oldItem is ReaderLoading && newItem is ReaderLoading) {
                oldItem == newItem
            } else if (oldItem is Content && newItem is Content) {
                oldItem == newItem
            } else false
        }
    }

    private val mDiffer = AsyncListDiffer(this, mDiffCallback)

    private var mReaderInfo: ReaderInfo? = null
        private set

    private fun getItem(@IntRange(from = 0) position: Int) = mDiffer.currentList[position]

    fun getCurrentList() = mDiffer.currentList

    fun submitList(contents: MutableList<Any?>, runnable: Runnable) = mDiffer.submitList(contents) {
       runnable.run()
    }

    inner class PageViewHolder(binding: BookComicPageRvBinding) : MangaCoilVH<BookComicPageRvBinding>(binding) {

       init {
           initComponent(binding.loading, binding.loadingText, binding.image, binding.retry)
       }

        fun onBind(item: Content) {
            loadImageAdjustWithRetry(when {
                item.mImageUrl.contains("c800x.") -> item.mImageUrl.replace("c800x.", "c${MangaXAccountConfig.mResolution}x.")
                item.mImageUrl.contains("c1200x.") -> item.mImageUrl.replace("c1200x.", "c${MangaXAccountConfig.mResolution}x.")
                item.mImageUrl.contains("c1500x.") -> item.mImageUrl.replace("c1500x.", "c${MangaXAccountConfig.mResolution}x.")
                else -> item.mImageUrl
            })
        }
    }

    inner class PageMoreViewHolder(val binding: BookComicLoadingRvBinding) : RecyclerView.ViewHolder(binding.root) {

        private var mRetryAnimator: ViewPropertyAnimator? = null
        private var mLoadingAnimator: ViewPropertyAnimator? = null

        init {
            binding.retry.doOnClickInterval {
                (getItem(absoluteAdapterPosition) as ReaderLoading).apply {
                    mRetryAnimator?.cancel()
                    mLoadingAnimator?.cancel()
                    mRetryAnimator = binding.retry.animateFadeOutGone()
                    mLoadingAnimator = binding.loading.animateFadeIn()
                    val isNext = mLoadNext == true
                    if (isNext) {
                        onRetry(mNextUuid ?: return@doOnClickInterval, isNext)
                    } else {
                        onRetry(mPrevUuid ?: return@doOnClickInterval, isNext)
                    }
                }
            }
        }

        fun onBind(item: ReaderLoading) {
            val message = item.mMessage
            if (message == null) {
                if (binding.retry.isGone) {
                    mRetryAnimator?.cancel()
                    mRetryAnimator = binding.retry.animateFadeIn()
                }
                binding.text.text = null
            } else {
                if (binding.retry.isVisible) {
                    mRetryAnimator?.cancel()
                    mRetryAnimator = binding.retry.animateFadeOutGone()
                }
                binding.text.text = message
            }
            if (binding.loading.isVisible) {
                mLoadingAnimator?.cancel()
                mLoadingAnimator = binding.loading.animateFadeOutGone()
            }

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            LOADING_VIEW -> PageMoreViewHolder(BookComicLoadingRvBinding.inflate(LayoutInflater.from(parent.context), parent,false))
            CONTENT_VIEW -> PageViewHolder(BookComicPageRvBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> error("Unknown view type!")
        }
    }

    override fun getItemCount(): Int = mDiffer.currentList.size

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)) {
            is Content -> CONTENT_VIEW
            is ReaderLoading -> LOADING_VIEW
            else -> error("Unknown view type!")
        }
    }

    override fun onBindViewHolder(vh: RecyclerView.ViewHolder, position: Int) {
        when(vh) {
            is PageViewHolder -> vh.onBind(getItem(position) as Content)
            is PageMoreViewHolder -> vh.onBind(getItem(position) as ReaderLoading)
        }
    }

    fun updateReaderInfo(info: ReaderInfo) { mReaderInfo = info }

}