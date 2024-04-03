package com.crow.module_book.ui.adapter.comic.reader

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.annotation.IntRange
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.app.app
import com.crow.base.tools.coroutine.launchDelay
import com.crow.base.tools.extensions.BASE_ANIM_300L
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.mangax.copymanga.MangaXAccountConfig
import com.crow.base.tools.extensions.immersionPadding
import com.crow.module_book.databinding.BookComicRvBinding
import com.crow.module_book.databinding.BookFragmentClassicIntentRvBinding
import com.crow.module_book.model.entity.comic.reader.ReaderPrevNextInfo
import com.crow.module_book.model.resp.comic_page.Content
import kotlinx.coroutines.Job

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Package: com.crow.module_book.ui.adapter
 * @Time: 2023/9/1 1:15
 * @Author: CrowForKotlin
 * @formatter:on
 **************************/
class ComicStandardRvAdapter(
    val mLifecycleOwner: LifecycleCoroutineScope,
    val onPrevNext: (ReaderPrevNextInfo) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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

    inner class BodyViewHolder(val binding: BookComicRvBinding) : ComicVH<BookComicRvBinding>(mLifecycleOwner, binding) {

        init {

            init(
                loading = binding.loading,
                loadingText = binding.loadingText,
                image = binding.subImage,
                retry = binding.retry,
            )

            initImageListener {
                val item = getItem(absoluteAdapterPosition)
                if (item is Content) {
                    (itemView.context as LifecycleOwner).launchDelay(BASE_ANIM_300L) {
                        onBind(when {
                            item.mImageUrl.contains("c800x.") -> item.mImageUrl.replace("c800x.", "c${MangaXAccountConfig.mResolution}x.")
                            item.mImageUrl.contains("c1200x.") -> item.mImageUrl.replace("c1200x.", "c${MangaXAccountConfig.mResolution}x.")
                            item.mImageUrl.contains("c1500x.") -> item.mImageUrl.replace("c1500x.", "c${MangaXAccountConfig.mResolution}x.")
                            else -> item.mImageUrl
                        })
                    }
                }
            }
        }

        fun onBind(url: String) {
            loadLongStriptImage(when {
                url.contains("c800x.") -> url.replace("c800x.", "c${MangaXAccountConfig.mResolution}x.")
                url.contains("c1200x.") -> url.replace("c1200x.", "c${MangaXAccountConfig.mResolution}x.")
                url.contains("c1500x.") -> url.replace("c1500x.", "c${MangaXAccountConfig.mResolution}x.")
                else -> url
            })
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

        fun onBind(item: ReaderPrevNextInfo) { binding.comicNext.text = item.mInfo }
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
        if (vh is BodyViewHolder) { vh.binding.subImage.recycle() }
        super.onViewRecycled(vh)
    }

    override fun onBindViewHolder(vh: RecyclerView.ViewHolder, position: Int) {
        when(vh) {
            is BodyViewHolder -> {
                vh.onBind((getItem(position) as Content).mImageUrl)
            }
            is IntentViewHolder -> vh.onBind(getItem(position) as ReaderPrevNextInfo)
        }
    }

    private fun getItem(@IntRange(from = 0) position: Int) = mDiffer.currentList[position]

    fun getCurrentList() = mDiffer.currentList
    fun submitList(pages: MutableList<Any>) = mDiffer.submitList(pages)
    fun submitList(pages: MutableList<Any>, callback: Runnable) = mDiffer.submitList(pages, callback)
}