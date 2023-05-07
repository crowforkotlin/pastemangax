package com.crow.base.copymanga.glide

import android.os.Handler
import android.os.Looper
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

/*
* 如果是在Rv中使用回调监听 那么需要重写onViewRecycled去清空数据
*     override fun onViewRecycled(vh: ViewHolder) {
*        super.onViewRecycled(vh)
*        vh.mAppGlideProgressFactory?.doRemoveListener()?.doClean()
*        vh.mAppGlideProgressFactory = null
*     }
* */

class AppGlideProgressFactory private constructor (private val mUrl: String) {

    init { mProgressManagerMap[mUrl] = this }

    companion object {

        // 存储映射Url : Instance
        private val mProgressManagerMap: HashMap<String, AppGlideProgressFactory> = hashMapOf()

        // MainLopper 用于在UI线程中执行...
        private var mHandler: Handler = Handler(Looper.getMainLooper())

        // 获取实例
        fun getGlideProgressFactory(url: String): AppGlideProgressFactory? {
            return mProgressManagerMap[url]
        }


        // 创建实例
        fun createGlideProgressListener(url: String, appOnGlideProgressListener: AppOnGlideProgressListener): AppGlideProgressFactory {
            return AppGlideProgressFactory(url).addListener { iUrl, isComplete, percentage, bytesRead, totalBytes -> appOnGlideProgressListener.doOnProgress(iUrl, isComplete, percentage, bytesRead, totalBytes) }
        }

        // 获取RequestListener 如果要使用进度监听务必加上此行 在回调结束前会移除对应实例数据，防止留下垃圾

        // 清空数据
        fun doReset() { mProgressManagerMap.clear() }

        // 获取进度字符串
        fun getProgressString(progress: Int) = "$progress%"
    }

    private var mProgressListener: AppOnGlideProgressListener? = null

    private var mProgressPercentage: Int = -1

    val mListener = object : AppGlideProgressResponseBody.InternalProgressListener {
        override fun onProgress(url: String, bytesRead: Long, totalBytes: Long) {
            mProgressPercentage = (bytesRead * 1f / totalBytes * 100f).toInt()
            val isComplete = mProgressPercentage >= 100
            mHandler.post { mProgressListener?.doOnProgress(url, isComplete, mProgressPercentage, bytesRead, totalBytes) }
            if (isComplete) {
                doClean(url)
                doRemoveListener()
            }
        }
    }

    inline fun<T> getRequestListener(crossinline failure: (catch: GlideException?) -> Boolean = { false }, crossinline ready: (resource: T) -> Boolean= { false }): RequestListener<T> {
        return object : RequestListener<T> {
            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<T>?, isFirstResource: Boolean) : Boolean {
                doClean()
                return failure(e)
            }
            override fun onResourceReady(resource: T, model: Any?, target: Target<T>?, dataSource: DataSource?, isFirstResource: Boolean) : Boolean {
                doClean()
                return ready(resource)
            }
        }
    }

    fun addListener(listener: AppOnGlideProgressListener): AppGlideProgressFactory {
        mProgressListener = listener
        return this
    }

    fun doRemoveListener(): AppGlideProgressFactory {
        mProgressListener = null
        return this
    }

    fun doClean(url: String = mUrl): AppGlideProgressFactory {
        mProgressManagerMap.remove(url)
        return this
    }
}
