package com.crow.module_main.di.glide

import android.text.TextUtils
import cc.shinichi.library.glide.progress.OnProgressListener
import cc.shinichi.library.tool.common.HttpUtil
import java.util.Collections

object AppProgressManager {

    private val listenersMap = Collections.synchronizedMap(HashMap<String, OnProgressListener>())

    val LISTENER = object : AppProgressResponseBody.InternalProgressListener {
        override fun onProgress(url: String?, bytesRead: Long, totalBytes: Long) {
            val onProgressListener = getProgressListener(url)
            val percentage = (bytesRead * 1f / totalBytes * 100f).toInt()
            val isComplete = percentage >= 100
            onProgressListener?.onProgress(url, isComplete, percentage, bytesRead, totalBytes)
            if (isComplete) {
                removeListener(url)
            }
        }
    }


    @JvmStatic
    fun addListener(url: String?, listener: OnProgressListener?) {
        if (!TextUtils.isEmpty(url)) {
            listenersMap[url] = listener
            listener?.onProgress(url, false, 1, 0, 0)
        }
    }

    private fun removeListener(url: String?) {
        if (!TextUtils.isEmpty(url)) {
            listenersMap.remove(url)
        }
    }

    private fun getProgressListener(url: String?): OnProgressListener? {
        return if (TextUtils.isEmpty(url) || listenersMap.isEmpty()) {
            null
        } else listenersMap[url?.let { HttpUtil.decode(it) }]
    }
}