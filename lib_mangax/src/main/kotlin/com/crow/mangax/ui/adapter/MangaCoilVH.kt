package com.crow.mangax.ui.adapter

import android.graphics.BitmapFactory
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
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.tools.extensions.log
import com.crow.mangax.copymanga.BaseStrings
import com.crow.mangax.copymanga.entity.AppConfig
import com.crow.mangax.copymanga.entity.CatlogConfig
import com.crow.mangax.copymanga.okhttp.AppProgressFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import okhttp3.HttpUrl.Companion.toHttpUrl

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

    fun loadImageWithRetry(url: String) {
        mRetry?.isGone = true
        mLoading.isInvisible = false
        mLoadingText.isInvisible = false
        mLoadingText.text = AppProgressFactory.PERCENT_0
        mAppProgressFactory?.removeProgressListener()?.remove()
        mAppProgressFactory = AppProgressFactory.createProgressListener(url) { _, _, percentage, _, _ -> mLoadingText.text = AppProgressFactory.formateProgress(percentage) }
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
                .data(url)
                .scale(Scale.FIT)
                .decoderFactory { source, option, _ -> Decoder { DecodeResult(drawable =BitmapFactory.decodeStream(source.source.source().inputStream()).toDrawable(option.context.resources), false) } }
                .target(mImage)
                .build()
        )
    }

    fun loadCoverImage(url: String) {
        val cover = if(CatlogConfig.mCoverOrinal) getOrignalCover(url) else url
        mLoading.isInvisible = false
        mLoadingText.isInvisible = false
        mLoadingText.text = AppProgressFactory.PERCENT_0
        mAppProgressFactory?.apply {
            removeProgressListener()
            remove()
        }
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
    private fun getImageUrl(url: String): String {
        return if ((AppConfig.getInstance()?.mApiSecret?.length ?: 0) >= 20 && CatlogConfig.mApiImageProxyEnable) {
            url.toHttpUrl() .newBuilder()
                .host(BaseStrings.URL.WUYA_API_IMAGE)
                .scheme(BaseStrings.URL.SCHEME_HTTPS)
                .build()
                .toString()
        } else {
            url
        }
    }
}