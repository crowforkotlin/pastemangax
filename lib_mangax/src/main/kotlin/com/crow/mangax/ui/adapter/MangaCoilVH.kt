package com.crow.mangax.ui.adapter

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import coil.decode.DecodeResult
import coil.decode.Decoder
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Scale
import com.crow.base.app.app
import com.crow.base.tools.coroutine.launchDelay
import com.crow.base.tools.extensions.BASE_ANIM_300L
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.tools.extensions.log
import com.crow.mangax.copymanga.MangaXAccountConfig
import com.crow.mangax.copymanga.entity.CatlogConfig
import com.crow.mangax.copymanga.getImageUrl
import com.crow.mangax.copymanga.okhttp.AppProgressFactory
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.OnImageEventListener
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.Job

open class MangaCoilVH<VB: ViewBinding>(val binding: VB) : LoadingVH<VB>(binding) {

    companion object {
        val mRegex = Regex("""\.(jpg|jpeg|png|gif|webp|jfif)(\.\d+x\d+\..+)""", RegexOption.IGNORE_CASE)
        val mRegexApiProxy = Regex("\\.", RegexOption.IGNORE_CASE)

        fun getOrignalCover(url: String): String {
            return url.replace(mRegex, ".$1")
        }
    }

    protected lateinit var mLoading: CircularProgressIndicator
    protected lateinit var mLoadingText: TextView
    protected lateinit var mImage: ImageView
    protected lateinit var mSubsamplingScaleImageView: SubsamplingScaleImageView
    protected lateinit var mRetry: MaterialButton

    fun initComponent(loading: CircularProgressIndicator, text: TextView, image: ImageView? = null, button: MaterialButton? = null, subsamplingScaleImageView: SubsamplingScaleImageView? = null) {
        mLoading = loading
        mLoadingText = text
        if (image != null) { mImage = image }
        if (button != null) { mRetry = button }
        if (subsamplingScaleImageView != null) { mSubsamplingScaleImageView = subsamplingScaleImageView }
    }

    fun loadImageWithRetry(url: String) {
        val imageUrl = getImageUrl(url)
        mRetry?.isGone = true
        mLoading.isInvisible = false
        mLoadingText.isInvisible = false
        mLoadingText.text = AppProgressFactory.PERCENT_0
        loading(imageUrl) { mLoadingText.text = it }
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
                    onSuccess = { _, _ ->
                        mLoading.isInvisible = true
                        mLoadingText.isInvisible = true
                        mRetry?.isGone = true
                        if (isNull) {
                            itemView.post { itemView.updateLayoutParams<ViewGroup.LayoutParams> { height = WRAP_CONTENT } }
                        } else {
                            itemView.updateLayoutParams<ViewGroup.LayoutParams> { height = WRAP_CONTENT }
                        }
                    },
                    onError = { _, _ ->
                        mLoading.isInvisible = true
                        mLoadingText.isInvisible = true
                        mRetry?.isVisible = true
                        mRetry?.doOnClickInterval {
                            mLoading.isInvisible = false
                            mLoadingText.isInvisible = false
                            it.mType.isGone = true
                            (itemView.context as LifecycleOwner).launchDelay(BASE_ANIM_300L) { loadImageWithRetry(url) }
                        }
                    },
                )
                .data(imageUrl)
                .scale(Scale.FIT)
                .decoderFactory { source, option, _ -> Decoder { DecodeResult(drawable =BitmapFactory.decodeStream(source.source.source().inputStream()).toDrawable(option.context.resources), false) } }
                .target {
                    mSubsamplingScaleImageView.setImage(ImageSource.Bitmap((it as BitmapDrawable).bitmap))
                }
                .build()
        )
    }

    fun loadCoverImage(url: String) {
        val cover = if(CatlogConfig.mCoverOrinal) getImageUrl(getOrignalCover(url)) else getImageUrl(url)
        mLoading.isInvisible = false
        mLoadingText.isInvisible = false
        mLoadingText.text = AppProgressFactory.PERCENT_0
        loading(cover) { mLoadingText.text = it }
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