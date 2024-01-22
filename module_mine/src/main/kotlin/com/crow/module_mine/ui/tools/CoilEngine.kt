package com.crow.module_mine.ui.tools

import android.content.Context
import android.widget.ImageView
import coil.imageLoader
import coil.request.ImageRequest
import com.crow.base.app.app
import com.luck.picture.lib.engine.ImageEngine
import com.luck.picture.lib.utils.ActivityCompatHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class CoilEngine private constructor() : ImageEngine {

    companion object { val INSTANCE by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { CoilEngine() } }

    private val mJob = SupervisorJob()
    private val mScope = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher() + mJob)

    override fun loadImage(context: Context, url: String, imageView: ImageView) {
        if (!ActivityCompatHelper.assertValidRequest(context)) return
        mScope.launch {
            app.imageLoader.execute(
                ImageRequest.Builder(context)
                    .data(url)
                    .target(imageView)
                    .build()
            )
        }
    }

    override fun loadImage(
        context: Context,
        imageView: ImageView,
        url: String,
        maxWidth: Int,
        maxHeight: Int
    ) {
        if (!ActivityCompatHelper.assertValidRequest(context)) return
        mScope.launch {
            app.imageLoader.execute(
                ImageRequest.Builder(context)
                    .data(url)
                    .target(imageView)
                    .size(maxWidth, maxHeight)
                    .build()
            )
        }
    }

    override fun loadAlbumCover(context: Context, url: String, imageView: ImageView) {
        if (!ActivityCompatHelper.assertValidRequest(context)) return
        mScope.launch {
            app.imageLoader.execute(
                ImageRequest.Builder(context)
                    .data(url)
                    .target(imageView)
                    .build()
            )
        }
    }

    override fun loadGridImage(context: Context, url: String, imageView: ImageView) {
        if (!ActivityCompatHelper.assertValidRequest(context)) return
        mScope.launch {
            app.imageLoader.execute(
                ImageRequest.Builder(context)
                    .data(url)
                    .target(imageView)
                    .build()
            )
        }
    }

    override fun pauseRequests(context: Context) {  }

    override fun resumeRequests(context: Context) {  }
}
