
package com.crow.module_book.ui.adapter.comic.reader

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.IntRange
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.tools.extensions.log
import com.crow.mangax.copymanga.BaseUserConfig
import com.crow.mangax.ui.adapter.MangaCoilVH
import com.crow.module_book.databinding.BookActivityComicButtonRvBinding
import com.crow.module_book.databinding.BookActivityComicRvBinding
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
class ComicStriptRvAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val LOADING_VIEW = 0
        private const val CONTENT_VIEW = 1
    }

    private val mDiffCallback: DiffUtil.ItemCallback<Any> = object : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return if (oldItem is ReaderLoading && newItem is ReaderLoading) {
                false
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

    inner class PageViewHolder(binding: BookActivityComicRvBinding) : MangaCoilVH<BookActivityComicRvBinding>(binding) {

       init {
           initComponent(binding.loading, binding.loadingText, binding.image, binding.retry)
       }

        fun onBind(item: Content) {
            loadImageWithRetry(when {
                item.mImageUrl.contains("c800x.") -> item.mImageUrl.replace("c800x.", "c${BaseUserConfig.RESOLUTION}x.")
                item.mImageUrl.contains("c1200x.") -> item.mImageUrl.replace("c1200x.", "c${BaseUserConfig.RESOLUTION}x.")
                item.mImageUrl.contains("c1500x.") -> item.mImageUrl.replace("c1500x.", "c${BaseUserConfig.RESOLUTION}x.")
                else -> item.mImageUrl
            })
        }
    }

    inner class PageMoreViewHolder(val binding: BookActivityComicButtonRvBinding) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(item: ReaderLoading) {
            binding.text.text = item.mMessage
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            LOADING_VIEW -> PageMoreViewHolder(BookActivityComicButtonRvBinding.inflate(LayoutInflater.from(parent.context), parent,false))
            CONTENT_VIEW -> PageViewHolder(BookActivityComicRvBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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

    override fun onViewRecycled(vh: RecyclerView.ViewHolder) {
        super.onViewRecycled(vh)
        when(vh) {
            is PageViewHolder -> {  vh.binding.root.layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT }
//            is PageMoreViewHolder { vh.binding}
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