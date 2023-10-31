package com.crow.base.copymanga.glide

import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

/**
 * ● Glide进度工厂
 * ● @author: CrowForKotlin
 * ● 2023-07-07 22:12:17 周五 下午
 */
class AppGlideProgressFactory private constructor (private val mUrl: String) {

    init { mProgressManagerMap[mUrl] = this }

    companion object {

        const val PERCENT_0 = "0%"

        // 存储映射Url : Instance
        private val mProgressManagerMap: HashMap<String, AppGlideProgressFactory> = hashMapOf()

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
        fun  getProgressString(progress: Int) = "$progress%"
    }

    private var mProgressListener: AppOnGlideProgressListener? = null

    private var pos: Int = -1

    private var mProgressPercentage: Int = -1

    val mOnProgressListener = object : AppGlideProgressResponseBody.InternalProgressListener {
        override fun onProgress(url: String, bytesRead: Long, totalBytes: Long) {
                mProgressPercentage = (bytesRead * 1f / totalBytes * 100f).toInt()
                val isComplete = mProgressPercentage >= 100
                mProgressListener?.doOnProgress(url, isComplete, mProgressPercentage, bytesRead, totalBytes)
                if (isComplete) {
                    onCleanCache(url)
                    onRemoveListener()
                }
        }
    }


    inline fun<T> getRequestListener(crossinline failure: (catch: GlideException?) -> Boolean = { false }, crossinline ready: (resource: T, dataSource: DataSource?) -> Boolean= { _, _ -> false }): RequestListener<T> {
        return object : RequestListener<T> {
            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<T>, isFirstResource: Boolean) : Boolean {
                onRemoveListener().onCleanCache()
                return failure(e)
            }
            override fun onResourceReady(resource: T & Any, model: Any, target: Target<T>, dataSource: DataSource, isFirstResource: Boolean) : Boolean {
                onRemoveListener().onCleanCache()
                return ready(resource, dataSource)
            }
        }
    }

    fun addListener(listener: AppOnGlideProgressListener): AppGlideProgressFactory {
        mProgressListener = listener
        return this
    }

    fun onRemoveListener(): AppGlideProgressFactory {
        mProgressListener = null
        return this
    }

    fun onCleanCache(url: String = mUrl): AppGlideProgressFactory {
        mProgressManagerMap.remove(url)
        return this
    }
}
