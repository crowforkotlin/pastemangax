package com.crow.module_book.ui.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.crow.base.copymanga.glide.AppGlideProgressFactory
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.animateFadeOut
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.ui.adapter.BaseGlideViewHolder
import com.crow.module_book.databinding.BookComicRvBinding
import com.crow.module_book.model.resp.comic_page.Content
import kotlinx.coroutines.delay
import org.koin.java.KoinJavaComponent

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_comic/src/main/kotlin/com/crow/module_comic/ui/adapter
 * @Time: 2023/3/15 16:42
 * @Author: CrowForKotlin
 * @Description: ComicInfoChapterRvAdapter
 * @formatter:on
 **************************/
class ComicRvAdapter(private var mComicContent: MutableList<Content> = mutableListOf()) : RecyclerView.Adapter<ComicRvAdapter.ViewHolder>() {

    private val mGenericTransitionOptions = KoinJavaComponent.getKoin().get<GenericTransitionOptions<Drawable>>()

    inner class ViewHolder(binding: BookComicRvBinding) : BaseGlideViewHolder<BookComicRvBinding>(binding)

    private fun ViewHolder.loadComicImage(position: Int) {

        val item = mComicContent[position]

        mAppGlideProgressFactory = AppGlideProgressFactory.createGlideProgressListener(item.mImageUrl) { _, _, percentage, _, _ ->
            rvBinding.comicRvProgressText.text = AppGlideProgressFactory.getProgressString(percentage)
        }

        Glide.with(itemView.context)
            .load(item.mImageUrl)
            .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)   // 指定图片尺寸
            .error {

            }
            .addListener(mAppGlideProgressFactory?.getRequestListener ({
                rvBinding.root.layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT
                rvBinding.comicRvRetry.isVisible = true
                rvBinding.comicRvRetry.doOnClickInterval(false) {
                    rvBinding.comicRvRetry.animateFadeOut()
                    loadComicImage(position)
                }
                false
            }))
            .transition(mGenericTransitionOptions.transition { _ ->
                rvBinding.root.layoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT
                rvBinding.comicRvRetry.isVisible = false
                rvBinding.comicRvImageView.animateFadeIn()
                rvBinding.comicRvProgressText.animateFadeOut().withEndAction { rvBinding.comicRvProgressText.alpha = 1f }
                rvBinding.comicRvLoading.animateFadeOut().withEndAction { rvBinding.comicRvLoading.alpha = 1f }
            })
            .into(rvBinding.comicRvImageView)
    }


    override fun onViewRecycled(vh: ViewHolder) {
        super.onViewRecycled(vh)
        vh.mAppGlideProgressFactory?.doRemoveListener()?.doClean()
        vh.mAppGlideProgressFactory = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(BookComicRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)).also {

        }
    }

    override fun getItemCount(): Int = mComicContent.size

    override fun onBindViewHolder(vh: ViewHolder, position: Int) {
        vh.loadComicImage(position)
    }

    suspend fun doNotify(newDataResult: MutableList<Content>, delayMs: Long = 1L) {
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