
package com.crow.module_book.ui.adapter.comic.reader

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.IntRange
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
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
import com.crow.mangax.copymanga.MangaXAccountConfig
import com.crow.mangax.copymanga.getImageUrl
import com.crow.mangax.copymanga.okhttp.AppProgressFactory
import com.crow.mangax.ui.adapter.MangaCoilVH
import com.crow.module_book.databinding.BookComicLoadingVerticalPageRvBinding
import com.crow.module_book.databinding.BookComicPagerPageRvBinding
import com.crow.module_book.model.entity.comic.reader.ReaderLoading
import com.crow.module_book.model.entity.comic.reader.ReaderPageLoading
import com.crow.module_book.model.resp.comic_page.Content
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.OnImageEventListener
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.davemorrissey.labs.subscaleview.decoder.SkiaPooledImageRegionDecoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_comic/src/main/kotlin/com/crow/module_comic/ui/adapter
 * @Time: 2023/3/15 16:42
 * @Author: CrowForKotlin
 * @Description: ComicInfoChapterRvAdapter
 * @formatter:on
 **************************/
class ComicPageVerticalRvAdapter(val mLifecycleOwner: LifecycleOwner, val onRetry: (uuid: String, isNext: Boolean) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val LOADING_VIEW = 0
        private const val CONTENT_VIEW = 1
    }

    inner class PageLoadingVH(val binding: BookComicLoadingVerticalPageRvBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.retryLeft.doOnClickInterval {
                val item = getItem(absoluteAdapterPosition)
                if (item is ReaderPageLoading) {
                    binding.retryLeft.animateFadeOut()
                        .withEndAction(object : Runnable {
                            override fun run() {
                                binding.retryLeft.isGone = true
                                binding.loadingLeft.animateFadeIn() .withEndAction { onRetry(item.mNextUuid ?: return@withEndAction, false) }
                            }
                        })
                }
            }
            binding.retryRight.doOnClickInterval {
                val item = getItem(absoluteAdapterPosition)
                if (item is ReaderPageLoading) {
                    binding.retryRight.animateFadeOut()
                        .withEndAction(object : Runnable {
                            override fun run() {
                                binding.retryRight.isGone = true
                                binding.loadingRight.animateFadeIn() .withEndAction { onRetry(item.mNextUuid ?: return@withEndAction, false) }
                            }
                        })
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
        private var mCurrentImage: String? = null
        private var mPrevJob: Job? = null

        init {
            binding.image.bindToLifecycle(mLifecycleOwner)
            binding.image.regionDecoderFactory = SkiaPooledImageRegionDecoder.Factory()
            binding.image.addOnImageEventListener(object : OnImageEventListener {
                override fun onImageLoaded() { }
                override fun onPreviewLoadError(e: Throwable) { }
                override fun onPreviewReleased() { }
                override fun onTileLoadError(e: Throwable) { }
                override fun onImageLoadError(e: Throwable) {
                    binding.loading.isInvisible = true
                    binding.loadingText.isInvisible = true
                    binding.retry.isVisible = true
                    binding.retry.doOnClickInterval {
                        binding.retry.isGone = true
                        binding.loading.isInvisible = false
                        binding.loadingText.isInvisible = false
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
                override fun onReady() {
                    binding.image.apply {
                        minimumScaleType = SubsamplingScaleImageView.SCALE_TYPE_CUSTOM
                        maxScale = 2f * maxOf(
                            width / sWidth.toFloat(),
                            height / sHeight.toFloat(),
                        )
                    }
                }
            })
        }

        fun onBind(url: String) {
            val imageUrl = getImageUrl(url)
            binding.apply {
                mPrevJob?.cancel()
                mPrevJob = mLifecycleOwner.lifecycleScope.launch {
                    image.recycle()
                    retry.isGone = true
                    loading.isInvisible = false
                    loadingText.isInvisible = false
                    loadingText.text = AppProgressFactory.PERCENT_0
                   async(Dispatchers.IO) {
                        app.imageLoader.execute(ImageRequest.Builder(image.context)
                            .addListener(url)
                            .data(imageUrl)
                            .decoderFactory { source, _, _ -> Decoder { DecodeResult(drawable =BitmapFactory.decodeStream(source.source.source().inputStream()).toDrawable(app.resources), false) } }
                            .build()
                        )
                    }.await().also {  result ->
                        result.drawable?.let { drawable ->
                            image.setImage(ImageSource.Bitmap((drawable as BitmapDrawable).bitmap, isCached = true)) }
                        }
                }
            }
        }

        private fun ImageRequest.Builder.addListener(imageUrl: String): ImageRequest.Builder {
            binding.apply {
                return listener(
                    onSuccess = { _, _ ->
                        loading.isInvisible = true
                        loadingText.isInvisible = true
                        retry.isGone = true
                    },
                    onError = { _, _ ->
                        loading.isInvisible = true
                        loadingText.isInvisible = true
                        retry.isVisible = true
                        retry.doOnClickInterval {
                            retry.isGone = true
                            loading.isInvisible = false
                            loadingText.isInvisible = false
                            (itemView.context as LifecycleOwner).launchDelay(BASE_ANIM_300L) { onBind(imageUrl) }
                        }
                    },
                )
            }
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

}