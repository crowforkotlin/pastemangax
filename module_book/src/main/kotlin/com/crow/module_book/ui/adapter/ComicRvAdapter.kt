
package com.crow.module_book.ui.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.IntRange
import androidx.core.view.isGone
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
import com.crow.base.ui.adapter.BaseGlideLoadingViewHolder
import com.crow.module_book.databinding.BookActivityComicButtonRvBinding
import com.crow.module_book.databinding.BookActivityComicRvBinding
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
        private const val PageLastView = 0
        private const val PageView = 1
    }

    private val mDiffCallback: DiffUtil.ItemCallback<Content> = object : DiffUtil.ItemCallback<Content>() {
        override fun areItemsTheSame(oldItem: Content, newItem: Content): Boolean {
            return oldItem == newItem
        }
        override fun areContentsTheSame(oldItem: Content, newItem: Content): Boolean {
            return oldItem.mIsLoading == newItem.mIsLoading && oldItem.mPrev == newItem.mPrev && oldItem.mNext == newItem.mNext && oldItem.mTips == newItem.mTips
        }
    }
    private val mDiffer = AsyncListDiffer(this, mDiffCallback)
    private fun getItem(@IntRange(from = 0) position: Int) = mDiffer.currentList[position]

    fun getCurrentList() = mDiffer.currentList

    fun submitList(contents: MutableList<Content>) = mDiffer.submitList(contents)

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

        fun onBind(item: Content, position: Int) {
            if (item.mIsLoading) {
                binding.striptLoading.isVisible = true
                binding.striptTipsCenter.isGone = true
            } else {
                binding.striptLoading.isInvisible = true
                binding.striptTipsCenter.isVisible = true
            }
            binding.striptTipsPrev.isVisible = item.mPrev != null
            binding.striptTipsNext.isVisible = item.mNext != null

            binding.striptTipsCenter.text = item.mTips
            binding.striptTipsPrev.text = item.mPrev
            binding.striptTipsNext.text = item.mNext
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            PageLastView ->PageMoreViewHolder(BookActivityComicButtonRvBinding.inflate(LayoutInflater.from(parent.context), parent,false))
            PageView -> PageViewHolder(BookActivityComicRvBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> error("Unknown view type!")
        }
    }

    override fun getItemCount(): Int = mDiffer.currentList.size

    override fun getItemViewType(position: Int) = if (getItem(position).mTips != null) PageLastView else PageView

    override fun onViewRecycled(vh: RecyclerView.ViewHolder) {
        super.onViewRecycled(vh)
        when(vh) {
            is PageViewHolder -> {  vh.rvBinding.root.layoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT }
        }
    }

    override fun onBindViewHolder(vh: RecyclerView.ViewHolder, position: Int) {
        when(vh) {
            is PageViewHolder -> vh.onBind(getItem(position))
            is PageMoreViewHolder -> vh.onBind(getItem(position), position)
        }
    }

}