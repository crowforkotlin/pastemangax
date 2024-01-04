package com.crow.mangax.copymanga.okhttp

fun interface AppProgressListener {
    fun doOnProgress(url: String, isComplete: Boolean, percentage: Int, bytesRead: Long, totalBytes: Long)
}