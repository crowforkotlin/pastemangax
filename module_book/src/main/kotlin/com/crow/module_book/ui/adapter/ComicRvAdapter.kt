
package com.crow.module_book.ui.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.IntRange
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.request.transition.DrawableCrossFadeTransition
import com.bumptech.glide.request.transition.NoTransition
import com.crow.base.copymanga.glide.AppGlideProgressFactory
import com.crow.base.tools.extensions.animateFadeOut
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.tools.extensions.logger
import com.crow.base.ui.adapter.BaseGlideLoadingViewHolder
import com.crow.module_book.databinding.BookActivityComicButtonRvBinding
import com.crow.module_book.databinding.BookActivityComicRvBinding
import com.crow.module_book.model.entity.ComicLoadMorePage
import com.crow.module_book.model.resp.comic_page.Content


/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_comic/src/main/kotlin/com/crow/module_comic/ui/adapter
 * @Time: 2023/3/15 16:42
 * @Author: CrowForKotlin
 * @Description: ComicInfoChapterRvAdapter
 * @formatter:on
 **************************/
class ComicRvAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val LoadingView = 0
        private const val ContentView = 1
    }

    private val mDiffCallback: DiffUtil.ItemCallback<Any> = object : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return if (oldItem is Content && newItem is Content) { oldItem.mImageUrl == newItem.mImageUrl } else true
        }
    }
    private val mDiffer = AsyncListDiffer(this, mDiffCallback)
    private fun getItem(@IntRange(from = 0) position: Int) = mDiffer.currentList[position]

    fun getCurrentList() = mDiffer.currentList

    fun submitList(contents: MutableList<Any>) = mDiffer.submitList(contents)

    inner class PageViewHolder(binding: BookActivityComicRvBinding) : BaseGlideLoadingViewHolder<BookActivityComicRvBinding>(binding) {

        fun onBind(item: Content) {

            rvBinding.comicRvLoading.isVisible = true
            rvBinding.comicRvProgressText.isVisible = true
            rvBinding.comicRvProgressText.text = AppGlideProgressFactory.PERCENT_0
            rvBinding.comicRvRetry.isVisible = false
            mAppGlideProgressFactory?.doRemoveListener()?.doClean()
            mAppGlideProgressFactory = AppGlideProgressFactory.createGlideProgressListener(item.mImageUrl) { _, _, percentage, _, _ ->
                rvBinding.comicRvProgressText.text = AppGlideProgressFactory.getProgressString(percentage)
            }

            Glide.with(itemView.context)
                .load(item.mImageUrl)
                .addListener(mAppGlideProgressFactory?.getRequestListener({
                    rvBinding.root.layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT
                    rvBinding.comicRvRetry.isVisible = true
                    rvBinding.comicRvRetry.doOnClickInterval(false) {
                        rvBinding.comicRvRetry.animateFadeOut()
                        onBind(item)
                    }
                    false
                },  { _, dataSource ->
                    rvBinding.root.layoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT
                    false
                }))
                .transition(GenericTransitionOptions<Drawable>().transition { dataSource, _ ->
                    val transition = if (dataSource == DataSource.REMOTE) {
                        rvBinding.comicRvLoading.isInvisible = true
                        rvBinding.comicRvProgressText.isInvisible = true

                        DrawableCrossFadeTransition(300, true)
                    } else {
                        rvBinding.comicRvLoading.isInvisible = true
                        rvBinding.comicRvProgressText.isInvisible = true
                        NoTransition()
                    }
                    transition
                })
                .into(rvBinding.comicRvImageView)
        }
    }

    inner class PageMoreViewHolder(val binding: BookActivityComicButtonRvBinding) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(item: ComicLoadMorePage, position: Int) {

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            LoadingView ->PageMoreViewHolder(BookActivityComicButtonRvBinding.inflate(LayoutInflater.from(parent.context), parent,false))
            ContentView -> PageViewHolder(BookActivityComicRvBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> error("Unknown view type!")
        }
    }

    override fun getItemCount(): Int = mDiffer.currentList.size

    override fun getItemViewType(position: Int): Int {
        val type = getItem(position)
        type.logger()
        return when(getItem(position)) {
            is Content -> {
                "isContent ${type}".logger()
                ContentView
            }
            is ComicLoadMorePage -> LoadingView
            else -> error("Unknown view type!")
        }
    }

    override fun onViewRecycled(vh: RecyclerView.ViewHolder) {
        super.onViewRecycled(vh)
        when(vh) {
            is PageViewHolder -> {  vh.rvBinding.root.layoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT }
        }
    }

    override fun onBindViewHolder(vh: RecyclerView.ViewHolder, position: Int) {
        when(vh) {
            is PageViewHolder -> vh.onBind(getItem(position) as Content)
            is PageMoreViewHolder -> vh.onBind(getItem(position) as ComicLoadMorePage, position)
        }
    }

}