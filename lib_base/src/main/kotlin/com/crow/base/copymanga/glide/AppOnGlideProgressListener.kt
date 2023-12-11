package com.crow.base.copymanga.glide

fun interface AppOnGlideProgressListener {
    fun doOnProgress(url: String, isComplete: Boolean, percentage: Int, bytesRead: Long, totalBytes: Long)
}