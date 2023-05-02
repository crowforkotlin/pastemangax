package com.crow.copymanga.di.glide

interface AppOnProgressListener {
    fun onProgress(url: String?, isComplete: Boolean, percentage: Int, bytesRead: Long, totalBytes: Long)
}