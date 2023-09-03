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
import com.crow.base.tools.extensions.animateFadeOut
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.tools.extensions.doOnInterval
import com.crow.base.tools.extensions.immersionPadding
import com.crow.base.ui.adapter.BaseGlideLoadingViewHolder
import com.crow.base.ui.view.event.BaseEvent
import com.crow.module_book.databinding.BookActivityComicRvBinding
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
class ComicClassicRvAdapter(val onPrevNext: (ReaderPrevNextInfo) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val Header = 0
        private const val Body = 1
        private const val Footer = 2
    }

    /**
     * ● Diff 回调
     *
     * ● 2023-09-02 20:06:41 周六 下午
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
     * ● 异步 Differ 实例
     *
     * ● 2023-09-02 20:06:53 周六 下午
     */
    private val mDiffer = AsyncListDiffer(this, mDiffCallback)

    private var mIsFirstInit: Boolean = true

    inner class BodyViewHolder(binding: BookActivityComicRvBinding) : BaseGlideLoadingViewHolder<BookActivityComicRvBinding>(binding) {
        fun onBind(position: Int) {
            val item = getItem(position) as Content
            if(binding.root.layoutParams.height == FrameLayout.LayoutParams.WRAP_CONTENT) binding.root.layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT
            binding.comicRvLoading.isVisible = true
            binding.comicRvProgressText.isVisible = true
            binding.comicRvProgressText.text = AppGlideProgressFactory.PERCENT_0
            binding.comicRvRetry.isVisible = false
            mAppGlideProgressFactory?.doRemoveListener()?.doClean()
            mAppGlideProgressFactory = AppGlideProgressFactory.createGlideProgressListener(item.mImageUrl) { _, _, percentage, _, _ ->
                binding.comicRvProgressText.text = AppGlideProgressFactory.getProgressString(percentage)
            }

            Glide.with(itemView.context)
                .load(item.mImageUrl)
                .addListener(mAppGlideProgressFactory?.getRequestListener(
                        failure = {

                            binding.comicRvRetry.isVisible = true
                            binding.comicRvRetry.doOnClickInterval(false) {
                                binding.comicRvRetry.animateFadeOut().withEndAction {
                                    onBind(position)
                                }
                            }
                            false
                        },
                        ready = { _, _ ->
                            binding.root.layoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT
                            false
                         }
                    )
                )
                .transition(GenericTransitionOptions<Drawable>().transition { dataSource, _ ->
                    val transition = if (dataSource == DataSource.REMOTE) {
                        binding.comicRvLoading.isInvisible = true
                        binding.comicRvProgressText.isInvisible = true
                        DrawableCrossFadeTransition(300, true)
                    } else {
                        binding.comicRvLoading.isInvisible = true
                        binding.comicRvProgressText.isInvisible = true
                        NoTransition()
                    }
                    mIsFirstInit = false
                    transition
                })
                .into(binding.comicRvImageView)
        }
    }

    inner class IntentViewHolder(val binding: BookFragmentClassicIntentRvBinding, isNext: Boolean) : RecyclerView.ViewHolder(binding.root) {

        init {
            if (isNext) immersionPadding(binding.root, paddingStatusBar = false, paddingNaviateBar = true)
            else immersionPadding(binding.root, paddingStatusBar = true, paddingNaviateBar = false)

            binding.comicNext.setOnClickListener {
                BaseEvent.getSIngleInstance().doOnInterval {
                    onPrevNext(getItem(absoluteAdapterPosition) as ReaderPrevNextInfo)
                }
            }
        }

        fun onBind(position: Int) {
            binding.comicNext.text = (getItem(absoluteAdapterPosition) as ReaderPrevNextInfo).mInfo
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            Header ->IntentViewHolder(BookFragmentClassicIntentRvBinding.inflate(LayoutInflater.from(parent.context), parent,false), false)
            Footer ->IntentViewHolder(BookFragmentClassicIntentRvBinding.inflate(LayoutInflater.from(parent.context), parent,false), true)
            Body -> BodyViewHolder(BookActivityComicRvBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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
            is BodyViewHolder -> { vh.binding.root.layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT }
            is IntentViewHolder -> { vh.binding.root.layoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT }
        }
    }

    override fun onBindViewHolder(vh: RecyclerView.ViewHolder, position: Int) {
        when(vh) {
            is BodyViewHolder -> vh.onBind(position)
            is IntentViewHolder -> vh.onBind(position)
        }
    }

    private fun getItem(@IntRange(from = 0) position: Int) = mDiffer.currentList[position]

    fun getCurrentList() = mDiffer.currentList

    fun submitList(pages: MutableList<Any>) = mDiffer.submitList(pages)
}