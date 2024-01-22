package com.crow.mangax.ui.adapter

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import coil.imageLoader
import coil.request.ImageRequest
import com.crow.base.app.app
import com.crow.base.tools.coroutine.launchDelay
import com.crow.base.tools.extensions.BASE_ANIM_300L
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.mangax.copymanga.entity.AppConfig
import com.crow.mangax.copymanga.okhttp.AppProgressFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator

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
        itemView.updateLayoutParams<ViewGroup.LayoutParams> { height = ViewGroup.LayoutParams.MATCH_PARENT }
        app.imageLoader.enqueue(
            ImageRequest.Builder(itemView.context)
                .listener(
                    onSuccess = { _, _ ->
                        mLoading.isInvisible = true
                        mLoadingText.isInvisible = true
                        mRetry?.isGone = true
                        itemView.updateLayoutParams<ViewGroup.LayoutParams> { height = ViewGroup.LayoutParams.WRAP_CONTENT }
                    },
                    onError = { _, _ ->
                        mLoading.isInvisible = true
                        mLoadingText.isInvisible = true
                        mRetry?.doOnClickInterval {
                            mLoading.isInvisible = false
                            mLoadingText.isInvisible = false
                            it.mType.isGone = true
                            (itemView.context as LifecycleOwner).launchDelay(BASE_ANIM_300L) { loadImageWithRetry(imageUrl) }
                        }
                    },
                )
                .data(imageUrl)
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
        val cover = if(AppConfig.mCoverOrinal) getOrignalCover(imageUrl) else imageUrl
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