package com.crow.mangax.ui.adapter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.doOnLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import coil.decode.BitmapFactoryDecoder
import coil.decode.DecodeResult
import coil.decode.Decoder
import coil.fetch.DrawableResult
import coil.imageLoader
import coil.load
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale
import com.crow.base.app.app
import com.crow.base.tools.coroutine.launchDelay
import com.crow.base.tools.extensions.BASE_ANIM_300L
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.tools.extensions.dp2px
import com.crow.base.tools.extensions.error
import com.crow.base.tools.extensions.info
import com.crow.base.tools.extensions.log
import com.crow.base.tools.extensions.px2dp
import com.crow.mangax.R
import com.crow.mangax.copymanga.entity.AppConfig
import com.crow.mangax.copymanga.entity.CatlogConfig
import com.crow.mangax.copymanga.okhttp.AppProgressFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

open class MangaCoilVH<VB: ViewBinding>(val binding: VB) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        val mRegex = Regex("""\.(jpg|jpeg|png|gif|webp|jfif)(\.\d+x\d+\..+)""", RegexOption.IGNORE_CASE)

        fun getOrignalCover(url: String): String {
            return url.replace(mRegex, ".$1")
        }
    }

    var mAppProgressFactory: AppProgressFactory? = null
    protected lateinit var mLoading: CircularProgressIndicator
    protected lateinit var mLoadingText: TextView
    protected lateinit var mImage: ImageView
    protected var mRetry: MaterialButton? = null

    fun initComponent(loading: CircularProgressIndicator, text: TextView, image: ImageView, button: MaterialButton? = null) {
        mLoading = loading
        mLoadingText = text
        mImage = image
        mRetry = button
    }

    fun loadImageWithRetry(imageUrl: String) {
        mLoading.isInvisible = false
        mLoadingText.isInvisible = false
        mRetry?.isGone = true
        mLoadingText.text = AppProgressFactory.PERCENT_0
        mAppProgressFactory?.removeProgressListener()?.remove()
        mAppProgressFactory = AppProgressFactory.createProgressListener(imageUrl) { _, _, percentage, _, _ -> mLoadingText.text = AppProgressFactory.formateProgress(percentage) }
        val isNull = itemView.tag == null
        if (isNull) {
            itemView.tag = itemView
            itemView.post { itemView.updateLayoutParams<ViewGroup.LayoutParams> { height = MATCH_PARENT } }
        } else {
            itemView.updateLayoutParams<ViewGroup.LayoutParams> { height = MATCH_PARENT }
        }

        app.imageLoader.enqueue(
            ImageRequest.Builder(itemView.context)
                .listener(
                    onSuccess = { _, result ->
                        mLoading.isInvisible = true
                        mLoadingText.isInvisible = true
                        mRetry?.isGone = true
                        if (isNull) {
                            itemView.post { itemView.updateLayoutParams<ViewGroup.LayoutParams> { height = WRAP_CONTENT } }
                        } else {
                            itemView.updateLayoutParams<ViewGroup.LayoutParams> { height = WRAP_CONTENT }
                        }
                    },
                    onError = { _, result ->
                        "CoilVH onError ${result.throwable.stackTraceToString()} \t ${result.request.data}".error()
                        mLoading.isInvisible = true
                        mLoadingText.isInvisible = true
                        mRetry?.isVisible = true
                        mRetry?.doOnClickInterval {
                            mLoading.isInvisible = false
                            mLoadingText.isInvisible = false
                            it.mType.isGone = true
                            (itemView.context as LifecycleOwner).launchDelay(BASE_ANIM_300L) { loadImageWithRetry(imageUrl) }
                        }
                    },
                )
                .data(imageUrl)
                .scale(Scale.FIT)
                .decoderFactory { source, option, _ -> Decoder { DecodeResult(drawable =BitmapFactory.decodeStream(source.source.source().inputStream()).toDrawable(option.context.resources), false) } }
                .target(mImage)
                .build()
        )

    }

    fun loadImageAdjustWithRetry(imageUrl: String) {
        mLoading.isInvisible = false
        mLoadingText.isInvisible = false
        mRetry?.isGone = true
        mLoadingText.text = AppProgressFactory.PERCENT_0
        mAppProgressFactory?.removeProgressListener()?.remove()
        mAppProgressFactory = AppProgressFactory.createProgressListener(imageUrl) { _, _, percentage, _, _ -> mLoadingText.text = AppProgressFactory.formateProgress(percentage) }
        app.imageLoader.enqueue(
            ImageRequest.Builder(itemView.context)
                .listener(
                    onSuccess = { _, result ->
                        mLoading.isInvisible = true
                        mLoadingText.isInvisible = true
                        mRetry?.isGone = true
                    },
                    onError = { _, result ->
                        "CoilVH onError ${result.throwable.stackTraceToString()} \t ${result.request.data}".error()
                        mLoading.isInvisible = true
                        mLoadingText.isInvisible = true
                        mRetry?.isVisible = true
                        mRetry?.doOnClickInterval {
                            mLoading.isInvisible = false
                            mLoadingText.isInvisible = false
                            it.mType.isGone = true
                            (itemView.context as LifecycleOwner).launchDelay(BASE_ANIM_300L) { loadImageWithRetry(imageUrl) }
                        }
                    },
                )
                .data(imageUrl)
                .scale(Scale.FIT)
                .decoderFactory { source, option, _ -> Decoder { DecodeResult(drawable =BitmapFactory.decodeStream(source.source.source().inputStream()).toDrawable(option.context.resources), false) } }
                .target(mImage)
                .build()
        )

    }

    fun loadCoverImage(imageUrl: String) {
        mLoading.isInvisible = false
        mLoadingText.isInvisible = false
        mLoadingText.text = AppProgressFactory.PERCENT_0
        mAppProgressFactory?.apply {
            removeProgressListener()
            remove()
        }
        val cover = if(CatlogConfig.mCoverOrinal) getOrignalCover(imageUrl) else imageUrl
        mAppProgressFactory = AppProgressFactory.createProgressListener(cover) { _, _, percentage, _, _ -> mLoadingText.text = AppProgressFactory.formateProgress(percentage) }
        app.imageLoader.enqueue(
            ImageRequest.Builder(itemView.context)
                .listener(
                    onSuccess = { _, _ ->
                        mLoading.isInvisible = true
                        mLoadingText.isInvisible = true
                    },
                    onError = { _, _ -> mLoadingText.text = "-1%" },
                )
                .data(cover)
                .target(mImage)
                .build()
        )
    }
}