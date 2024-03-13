
package com.crow.module_book.ui.adapter.comic.reader

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.IntRange
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.decode.DecodeResult
import coil.decode.Decoder
import coil.imageLoader
import coil.request.ImageRequest
import com.crow.base.app.app
import com.crow.base.tools.coroutine.launchDelay
import com.crow.base.tools.extensions.BASE_ANIM_300L
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.animateFadeOut
import com.crow.base.tools.extensions.animateFadeOutGone
import com.crow.base.tools.extensions.animateFadeOutInVisibility
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.tools.extensions.error
import com.crow.base.tools.extensions.log
import com.crow.mangax.copymanga.MangaXAccountConfig
import com.crow.mangax.copymanga.okhttp.AppProgressFactory
import com.crow.mangax.ui.adapter.MangaCoilVH
import com.crow.module_book.databinding.BookComicLoadingVerticalPageRvBinding
import com.crow.module_book.databinding.BookComicPagerPageRvBinding
import com.crow.module_book.model.entity.comic.reader.ReaderLoading
import com.crow.module_book.model.entity.comic.reader.ReaderPageLoading
import com.crow.module_book.model.resp.comic_page.Content
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.OnImageEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import kotlin.system.measureTimeMillis


/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_comic/src/main/kotlin/com/crow/module_comic/ui/adapter
 * @Time: 2023/3/15 16:42
 * @Author: CrowForKotlin
 * @Description: ComicInfoChapterRvAdapter
 * @formatter:on
 **************************/
class ComicPageVerticalRvAdapter(val onRetry: (uuid: String, isNext: Boolean) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val LOADING_VIEW = 0
        private const val CONTENT_VIEW = 1
    }

    private val mScope = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())

    inner class PageLoadingVH(val binding: BookComicLoadingVerticalPageRvBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.retryLeft.doOnClickInterval {
                val item = getItem(absoluteAdapterPosition)
                if (item is ReaderPageLoading) {
                    binding.retryLeft.animateFadeOut()
                        .withEndAction {
                            binding.retryLeft.isGone = true
                            binding.loadingLeft.animateFadeIn()
                                .withEndAction {
                                    onRetry(item.mPrevUuid ?: return@withEndAction, false)
                                }
                        }
                }
            }
            binding.retryRight.doOnClickInterval {
                val item = getItem(absoluteAdapterPosition)
                if (item is ReaderPageLoading) {
                    binding.retryRight.animateFadeOut()
                        .withEndAction {
                            binding.retryRight.isGone = true
                            binding.loadingRight.animateFadeIn()
                                .withEndAction {
                                    onRetry(item.mNextUuid ?: return@withEndAction, false)
                                }
                        }
                }
            }
        }

        fun onBind(item: ReaderPageLoading) {
            val prev = item.mPrevMessage
            val next = item.mNextMessage
            if (binding.loadingLeft.isVisible) binding.loadingLeft.animateFadeOutGone()
            if (binding.loadingRight.isVisible) binding.loadingRight.animateFadeOutGone()
            if (prev == null) {
                if (binding.retryLeft.isGone) binding.retryLeft.animateFadeIn().withEndAction {
                    if (binding.textLeft.isVisible) binding.textLeft.animateFadeOutInVisibility()
                }
            } else {
                binding.textLeft.text = prev
                if (binding.textLeft.isInvisible) binding.textLeft.animateFadeIn().withEndAction {
                    if (binding.retryLeft.isVisible) binding.retryLeft.animateFadeOutGone()
                }
            }

            if (next == null) {
                if (binding.retryRight.isGone) binding.retryRight.animateFadeIn().withEndAction {
                    if (binding.textRight.isVisible) binding.textRight.animateFadeOutInVisibility()
                }
            } else {
                binding.textRight.text = next
                if (binding.textRight.isInvisible) binding.textRight.animateFadeIn().withEndAction {
                    if (binding.retryRight.isVisible) binding.retryRight.animateFadeOutGone()
                }
            }
        }
    }

    inner class PageViewHolder(binding: BookComicPagerPageRvBinding) : MangaCoilVH<BookComicPagerPageRvBinding>(binding) {

        init {
            binding.image.addOnImageEventListener(object : OnImageEventListener {
                override fun onImageLoadError(e: Throwable) { }
                override fun onImageLoaded() { }
                override fun onPreviewLoadError(e: Throwable) { }
                override fun onPreviewReleased() { }
                override fun onReady() {
                    binding.image.maxScale = 2f * maxOf(
                        binding.image.width / binding.image.sWidth.toFloat(),
                        binding.image.height / binding.image.sHeight.toFloat(),
                    )
                    binding.image.animateFadeIn()
                }
                override fun onTileLoadError(e: Throwable) { }
            })
        }


        fun onBind(imageUrl: String) {
            binding.loading.isInvisible = false
            binding.loadingText.isInvisible = false
            binding.retry.isGone = true
            binding.loadingText.text = AppProgressFactory.PERCENT_0
            binding.image.recycle()
            binding.image.setImage(ImageSource.Bitmap(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)))
            mAppProgressFactory?.removeProgressListener()?.remove()
            mAppProgressFactory = AppProgressFactory.createProgressListener(imageUrl) { _, _, percentage, _, _ -> binding.loadingText.text = AppProgressFactory.formateProgress(percentage) }
            app.imageLoader.enqueue(
                ImageRequest.Builder(itemView.context)
                    .listener(
                        onSuccess = { _, _ ->
                            binding.loading.isInvisible = true
                            binding.loadingText.isInvisible = true
                            binding.retry.isGone = true
                        },
                        onError = { _, _ ->
                            binding.loading.isInvisible = true
                            binding.loadingText.isInvisible = true
                            binding.retry.isVisible = true
                            binding.retry.doOnClickInterval {
                                binding.loading.isInvisible = false
                                binding.loadingText.isInvisible = false
                                it.mType.isGone = true
                                (itemView.context as LifecycleOwner).launchDelay(BASE_ANIM_300L) { onBind(imageUrl) }
                            }
                        },
                    )
                    .data(imageUrl)
                    .decoderFactory { source, option, _ ->
                        Decoder {
                            val bitmap = BitmapFactory.decodeStream(source.source.source().inputStream())
                            DecodeResult(drawable =bitmap.toDrawable(app.resources), false)
                        }
                    }
                    .target {
                        mScope.launch {
                            val bitmap = it.toBitmap()
                            binding.image.post { binding.image.setImage(ImageSource.Bitmap(bitmap)) }
                        }
                    }
                    .build()
            )
        }
    }

    private val mDiffCallback: DiffUtil.ItemCallback<Any> = object : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return if (oldItem is ReaderPageLoading && newItem is ReaderPageLoading) {
                newItem.mChapterID == oldItem.mChapterID
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

    private fun getItem(@IntRange(from = 0) position: Int) = mDiffer.currentList[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            LOADING_VIEW -> PageLoadingVH(BookComicLoadingVerticalPageRvBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            CONTENT_VIEW -> PageViewHolder(BookComicPagerPageRvBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> error("unknow view type!")
        }
    }

    override fun getItemCount(): Int = mDiffer.currentList.size

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)) {
            is Content -> CONTENT_VIEW
            is ReaderPageLoading -> LOADING_VIEW
            else -> error("unknow view type!")
        }
    }

    override fun onBindViewHolder(vh: RecyclerView.ViewHolder, position: Int) {
        when(vh) {
            is PageViewHolder -> {
                val item = getItem(position) as Content
                vh.onBind(when {
                    item.mImageUrl.contains("c800x.") -> item.mImageUrl.replace("c800x.", "c${MangaXAccountConfig.mResolution}x.")
                    item.mImageUrl.contains("c1200x.") -> item.mImageUrl.replace("c1200x.", "c${MangaXAccountConfig.mResolution}x.")
                    item.mImageUrl.contains("c1500x.") -> item.mImageUrl.replace("c1500x.", "c${MangaXAccountConfig.mResolution}x.")
                    else -> item.mImageUrl
                })
            }
            is PageLoadingVH -> { vh.onBind(getItem(position) as ReaderPageLoading) }
        }
    }

    fun getCurrentList() = mDiffer.currentList

    fun submitList(contents: MutableList<Any>, runnable: Runnable) = mDiffer.submitList(contents) { runnable.run() }

    fun onDestroy() { mScope.cancel() }
}