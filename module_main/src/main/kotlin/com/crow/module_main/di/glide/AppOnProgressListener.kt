package com.crow.module_main.di.glide

interface AppOnProgressListener {
    fun onProgress(url: String?, isComplete: Boolean, percentage: Int, bytesRead: Long, totalBytes: Long)
}