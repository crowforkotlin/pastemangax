package com.crow.module_book.ui.adapter.comic.reader

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import coil.decode.DecodeResult
import coil.decode.Decoder
import coil.imageLoader
import coil.request.ImageRequest
import com.crow.base.app.app
import com.crow.base.tools.coroutine.launchDelay
import com.crow.base.tools.extensions.BASE_ANIM_300L
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.mangax.copymanga.getImageUrl
import com.crow.mangax.copymanga.okhttp.AppProgressFactory
import com.crow.mangax.ui.adapter.LoadingVH
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.OnImageEventListener
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.davemorrissey.labs.subscaleview.decoder.SkiaPooledImageRegionDecoder
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * ● ComicVH
 *
 * ● 2024/3/28 23:14
 * @author crowforkotlin
 * @formatter:on
 */
open class ComicVH<VB : ViewBinding>(val lifecycleCoroutineScope: LifecycleCoroutineScope, binding: VB) : LoadingVH<VB>(binding) {

    private lateinit var mLoading: CircularProgressIndicator
    private lateinit var mLoadingText: TextView
    private lateinit var mSubsamplingScaleImageView: SubsamplingScaleImageView
    private lateinit var mRetry: MaterialButton
    private var mLoadingJob: Job? = null
    val mHeight by lazy { itemView.resources.displayMetrics.heightPixels / 3 }

    fun init(
        loading: CircularProgressIndicator,
        loadingText: TextView,
        image: SubsamplingScaleImageView,
        retry: MaterialButton
    ) {
        mLoading = loading
        mLoadingText = loadingText
        mRetry = retry
        mSubsamplingScaleImageView = image
        mSubsamplingScaleImageView.regionDecoderFactory = SkiaPooledImageRegionDecoder.Factory()
    }

    fun initImageListener(onRetry: () -> Unit) {
        mSubsamplingScaleImageView.addOnImageEventListener(object : OnImageEventListener {
            override fun onImageLoaded() {}
            override fun onPreviewLoadError(e: Throwable) {}
            override fun onPreviewReleased() {}
            override fun onTileLoadError(e: Throwable) {}
            override fun onImageLoadError(e: Throwable) {
                mLoading.isInvisible = true
                mLoadingText.isInvisible = true
                mRetry.isVisible = true
                mRetry.doOnClickInterval {
                    mRetry.isGone = true
                    mLoading.isInvisible = false
                    mLoadingText.isInvisible = false
                    onRetry()
                }
            }

            override fun onReady() {
//                itemView.updateLayoutParams<ViewGroup.LayoutParams> { height = ViewGroup.LayoutParams.WRAP_CONTENT }
            }
        })
    }

    fun loadLongStriptImage(link: String) {
        val imageLink = getImageUrl(link)
        mLoadingJob?.cancel()
        mLoadingJob = lifecycleCoroutineScope.launch {
            initComponent(imageLink)
            val init = initItemViewHeight()
            async(Dispatchers.IO) {
                app.imageLoader.execute(
                    ImageRequest.Builder(itemView.context)
                        .addListener(imageLink, init)
                        .data(imageLink)
                        .decoderFactory { source, _, _ ->
                            Decoder {
                                DecodeResult(drawable = BitmapFactory.decodeStream(source.source.source().inputStream()).toDrawable(app.resources), false)
                            }
                        }
                        .build()
                )
            }.await().also { result ->
                result.drawable?.let { drawable ->
                    mSubsamplingScaleImageView.setImage(ImageSource.Bitmap((drawable as BitmapDrawable).bitmap, isCached = true))
                }
            }
        }
    }
    fun loadPageImage(link: String) {
        val imageLink = getImageUrl(link)
        mLoadingJob?.cancel()
        mLoadingJob = lifecycleCoroutineScope.launch {
            initComponent(imageLink)
            async(Dispatchers.IO) {
                app.imageLoader.execute(
                    ImageRequest.Builder(itemView.context)
                        .addListener(imageLink)
                        .data(imageLink)
                        .decoderFactory { source, _, _ ->
                            Decoder {
                                DecodeResult(drawable = BitmapFactory.decodeStream(source.source.source().inputStream()).toDrawable(app.resources), false)
                            }
                        }
                        .build()
                )
            }.await().also { result ->
                result.drawable?.let { drawable ->
                    mSubsamplingScaleImageView.setImage(ImageSource.Bitmap((drawable as BitmapDrawable).bitmap, isCached = true))
                }
            }
        }
    }

    private fun initItemViewHeight(): Boolean {
        val isInitTaskHeight = itemView.tag == null
        if (isInitTaskHeight) {
            itemView.tag = itemView
            itemView.post {
                itemView.updateLayoutParams<ViewGroup.LayoutParams> {
                    height = ViewGroup.LayoutParams.MATCH_PARENT
                }
            }
        } else {
            itemView.updateLayoutParams<ViewGroup.LayoutParams> {
                height = ViewGroup.LayoutParams.MATCH_PARENT
            }
        }
        return isInitTaskHeight
    }

    private fun initComponent(link: String) {
        mSubsamplingScaleImageView.recycle()
        mRetry.isGone = true
        mLoading.isInvisible = false
        mLoadingText.isInvisible = false
        mLoadingText.text = AppProgressFactory.PERCENT_0
        loading(link) { mLoadingText.text = it }
    }

    private fun ImageRequest.Builder.addListener(
        imageLink: String,
        isInit: Boolean
    ): ImageRequest.Builder {
        return listener(
            onSuccess = { _, _ ->
                setCompoenntCompleteState()
                setItemViewHeight(isInit)
            },
            onError = { _, _ ->
                setCompoenntCompleteState()
                setRetryListener(imageLink)
            },
        )
    }

    private fun ImageRequest.Builder.addListener(link: String): ImageRequest.Builder {
        return listener(
            onSuccess = { _, _ -> setCompoenntCompleteState() },
            onError = { _, _ ->
                setCompoenntCompleteState()
//                setRetryListener(link)
            }
        )

    }

    private fun setRetryListener(link: String) {
        mRetry.doOnClickInterval {
            mRetry.isGone = true
            mLoading.isInvisible = false
            mLoadingText.isInvisible = false
            (itemView.context as LifecycleOwner).launchDelay(BASE_ANIM_300L) {
                loadLongStriptImage(link)
            }
        }
    }

    private fun setItemViewHeight(isNull: Boolean) {
        if (isNull) {
            itemView.post {
                itemView.updateLayoutParams<ViewGroup.LayoutParams> {
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                }
            }
        } else {
            itemView.updateLayoutParams<ViewGroup.LayoutParams> {
                height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
        }
    }

    private fun setCompoenntCompleteState() {
        mLoading.isInvisible = true
        mLoadingText.isInvisible = true
        mRetry.isGone = true
    }
}