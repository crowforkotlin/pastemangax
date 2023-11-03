
package com.crow.module_book.ui.adapter.comic.reader

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
import com.crow.base.tools.extensions.animateFadeIn
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
class ComicStriptRvAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val LoadingView = 0
        private const val ContentView = 1
    }

    private val mDiffCallback: DiffUtil.ItemCallback<Content> = object : DiffUtil.ItemCallback<Content>() {
        override fun areItemsTheSame(oldItem: Content, newItem: Content): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Content, newItem: Content): Boolean {
            return oldItem.mImageUrl == newItem.mImageUrl
        }
    }

    private val mDiffer = AsyncListDiffer(this, mDiffCallback)

    private fun getItem(@IntRange(from = 0) position: Int) = mDiffer.currentList[position]

    fun getCurrentList() = mDiffer.currentList

    fun submitList(contents: MutableList<Content?>) = mDiffer.submitList(contents)

    inner class PageViewHolder(binding: BookActivityComicRvBinding) : BaseGlideLoadingViewHolder<BookActivityComicRvBinding>(binding) {

        fun onBind(item: Content) {

            binding.loading.isVisible = true
            binding.loadingText.isVisible = true
            binding.loadingText.text = AppGlideProgressFactory.PERCENT_0
            binding.retry.isVisible = false
            mAppGlideProgressFactory?.onRemoveListener()?.onCleanCache()
            mAppGlideProgressFactory = AppGlideProgressFactory.createGlideProgressListener(item.mImageUrl!!) { _, _, percentage, _, _ ->
                binding.loadingText.text = AppGlideProgressFactory.getProgressString(percentage)
            }

            Glide.with(itemView.context)
                .load(item.mImageUrl)
                .addListener(mAppGlideProgressFactory?.getRequestListener({
                    binding.root.layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT
                    binding.retry.animateFadeIn()
                    binding.loading.animateFadeOut()
                    binding.loadingText.animateFadeOut()
                    binding.retry.doOnClickInterval(false) {
                        binding.retry.animateFadeOut()
                        binding.loading.animateFadeIn()
                        binding.loadingText.animateFadeIn()
                        onBind(item)
                    }
                    false
                },  { _, dataSource ->
                    binding.root.layoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT
                    false
                }))
                .transition(GenericTransitionOptions<Drawable>().transition { dataSource, _ ->
                    val transition = if (dataSource == DataSource.REMOTE) {
                        binding.loading.isInvisible = true
                        binding.loadingText.isInvisible = true

                        DrawableCrossFadeTransition(300, true)
                    } else {
                        binding.loading.isInvisible = true
                        binding.loadingText.isInvisible = true
                        NoTransition()
                    }
                    transition
                })
                .into(binding.image)
        }
    }

    inner class PageMoreViewHolder(val binding: BookActivityComicButtonRvBinding) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(position: Int) {

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
        return when(getItem(position)) {
            is Content -> ContentView
            else -> error("Unknown view type!")
        }
    }

    override fun onViewRecycled(vh: RecyclerView.ViewHolder) {
        super.onViewRecycled(vh)
        when(vh) {
            is PageViewHolder -> {  vh.binding.root.layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT }
        }
    }

    override fun onBindViewHolder(vh: RecyclerView.ViewHolder, position: Int) {
        when(vh) {
            is PageViewHolder -> vh.onBind(getItem(position) as Content)
        }
    }

}