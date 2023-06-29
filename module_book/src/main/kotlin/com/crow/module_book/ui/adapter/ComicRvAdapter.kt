
package com.crow.module_book.ui.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.request.transition.DrawableCrossFadeTransition
import com.bumptech.glide.request.transition.NoTransition
import com.crow.base.copymanga.glide.AppGlideProgressFactory
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.animateFadeOut
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.adapter.BaseGlideLoadingViewHolder
import com.crow.module_book.R
import com.crow.module_book.databinding.BookActivityComicButtonRvBinding
import com.crow.module_book.databinding.BookActivityComicRvBinding
import com.crow.module_book.model.resp.comic_page.Content
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.delay


/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_comic/src/main/kotlin/com/crow/module_comic/ui/adapter
 * @Time: 2023/3/15 16:42
 * @Author: CrowForKotlin
 * @Description: ComicInfoChapterRvAdapter
 * @formatter:on
 **************************/
class ComicRvAdapter(
    private var mComicContent: MutableList<Content?> = mutableListOf(),
    val mHasNext: Boolean,
    val mHasPrev: Boolean,
    val doOnNext: Runnable
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val NextChapter = 0
        private const val PageView = 1
    }

    private var mIsFirstInit: Boolean = true

    inner class PageViewHolder(binding: BookActivityComicRvBinding) : BaseGlideLoadingViewHolder<BookActivityComicRvBinding>(binding)

    inner class ButtonViewHolder(binding: BookActivityComicButtonRvBinding) : RecyclerView.ViewHolder(binding.root) {
        val mNext: MaterialButton = binding.comicNext
    }

    private fun RecyclerView.ViewHolder.loadComicImage(position: Int) {
        when (this) {
            is PageViewHolder -> {
                val item = mComicContent[position] ?: return
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
                            loadComicImage(position)
                        }
                        false
                    },  { _, _ ->
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
                        mIsFirstInit = false
                        transition
                    })
                    .into(rvBinding.comicRvImageView)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            NextChapter ->ButtonViewHolder(BookActivityComicButtonRvBinding.inflate(LayoutInflater.from(parent.context), parent,false)).also { vh ->
                if (!mHasNext) vh.mNext.text = parent.context.getString(R.string.book_no_next)
                vh.mNext.doOnClickInterval {
                    if(!mHasNext) toast(parent.context.getString(R.string.book_no_next))
                    else doOnNext.run()
                }
            }
            PageView -> PageViewHolder(BookActivityComicRvBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> error("Unknown view type!")
        }
    }

    override fun getItemCount(): Int = mComicContent.size

    override fun getItemViewType(position: Int): Int {
        return if (position == mComicContent.size - 1) NextChapter else PageView
    }

    override fun onViewRecycled(vh: RecyclerView.ViewHolder) {
        super.onViewRecycled(vh)
        when(vh) {
            is PageViewHolder -> { vh.rvBinding.root.layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT }
        }
    }

    override fun onBindViewHolder(vh: RecyclerView.ViewHolder, position: Int) {
        vh.loadComicImage(position)
    }

    suspend fun doNotify(newDataResult: MutableList<Content?>, delayMs: Long = 1L) {
        val isCountSame = itemCount == newDataResult.size
        if (isCountSame) mComicContent = newDataResult
        else if(itemCount != 0) {
            notifyItemRangeRemoved(0, itemCount)
            mComicContent.clear()
            delay(BASE_ANIM_200L)
        }
        newDataResult.forEachIndexed { index, data ->
            if (!isCountSame) {
                mComicContent.add(data)
                notifyItemInserted(index)
            } else notifyItemChanged(index)
            delay(delayMs)
        }
    }
}