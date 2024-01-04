package com.crow.mangax.copymanga.okhttp

import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.crow.base.tools.extensions.log

/**
 * ● Okhttp进度加载
 *
 * ● 2023-07-07 22:12:17 周五 下午
 * @author crowforkotlin
 */
class AppProgressFactory private constructor (private val mUrl: String) {

    companion object {

        const val PERCENT_0 = "0%"

        /**
         * ● 存储映射Url : Instance
         *
         * ● 2024-01-05 01:38:02 周五 上午
         * @author crowforkotlin
         */
        private val mProgressManagerMap: HashMap<String, AppProgressFactory> = hashMapOf()

        /**
         * ● 获取实例
         *
         * ● 2024-01-05 01:37:51 周五 上午
         * @author crowforkotlin
         */
        fun getProgressFactory(url: String): AppProgressFactory? {
            return mProgressManagerMap[url]
        }

        /**
         * ● 创建实例
         *
         * ● 2024-01-05 01:37:19 周五 上午
         * @author crowforkotlin
         */
        fun createProgressListener(url: String, appOnGlideProgressListener: AppProgressListener): AppProgressFactory {
            return AppProgressFactory(url).setProgressListenerse { iUrl, isComplete, percentage, bytesRead, totalBytes -> appOnGlideProgressListener.doOnProgress(iUrl, isComplete, percentage, bytesRead, totalBytes) }
        }

        /**
         * ● 清空数据
         *
         * ● 2024-01-05 01:36:05 周五 上午
         * @author crowforkotlin
         */
        fun clear() { mProgressManagerMap.clear() }

        /**
         * ● 格式化字符串
         *
         * ● 2024-01-05 01:36:38 周五 上午
         * @author crowforkotlin
         */
        fun formateProgress(progress: Int) = "$progress%"
    }

    /**
     * ● 监听回调
     *
     * ● 2024-01-05 01:39:01 周五 上午
     * @author crowforkotlin
     */
    private var mProgressListener: AppProgressListener? = null

    /**
     * ● 进度百分比
     *
     * ● 2024-01-05 01:39:23 周五 上午
     * @author crowforkotlin
     */
    private var mProgressPercentage: Int = -1

    /**
     * ● 进度监听实现类
     *
     * ● 2024-01-05 01:39:41 周五 上午
     * @author crowforkotlin
     */
    val mRequestProgressListener = object : AppProgressResponseBody.InternalProgressListener {
        override fun onProgress(url: String, bytesRead: Long, totalBytes: Long) {
            mProgressPercentage = (bytesRead * 1f / totalBytes * 100f).toInt()
            val isComplete = mProgressPercentage >= 100
            mProgressListener?.doOnProgress(url, isComplete, mProgressPercentage, bytesRead, totalBytes)
            if (isComplete) {
                remove(url)
                removeProgressListener()
            }
        }
    }

    /**
     * ● 初始化路径
     *
     * ● 2024-01-05 01:42:30 周五 上午
     * @author crowforkotlin
     */
    init { mProgressManagerMap[mUrl] = this }

    /**
     * ● Glide监听扩展
     *
     * ● 2024-01-05 01:43:11 周五 上午
     * @author crowforkotlin
     */
    inline fun<T> getGlideRequestListener(crossinline failure: (catch: GlideException?) -> Boolean = { false }, crossinline ready: (resource: T, dataSource: DataSource?) -> Boolean= { _, _ -> false }): RequestListener<T> {
        return object : RequestListener<T> {
            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<T>, isFirstResource: Boolean) : Boolean {
                removeProgressListener().remove()
                return failure(e)
            }
            override fun onResourceReady(resource: T & Any, model: Any, target: Target<T>, dataSource: DataSource, isFirstResource: Boolean) : Boolean {
                removeProgressListener().remove()
                return ready(resource, dataSource)
            }
        }
    }

    /**
     * ● 设置进度监听器
     *
     * ● 2024-01-05 01:33:07 周五 上午
     * @author crowforkotlin
     */
    fun setProgressListenerse(listener: AppProgressListener): AppProgressFactory {
        mProgressListener = listener
        return this
    }

    /**
     * ● 移除进度回调监听器
     *
     * ● 2024-01-05 01:32:19 周五 上午
     * @author crowforkotlin
     */
    fun removeProgressListener(): AppProgressFactory {
        mProgressListener = null
        return this
    }

    /**
     * ● 移除Key
     *
     * ● 2024-01-05 01:31:21 周五 上午
     * @author crowforkotlin
     */
    fun remove(url: String = mUrl): AppProgressFactory {
        mProgressManagerMap.remove(url)
        return this
    }
}
