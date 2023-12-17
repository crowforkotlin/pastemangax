package com.crow.mangax.copymanga.glide

import android.os.Handler
import android.os.Looper
import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.Source
import okio.buffer

class AppGlideProgressResponseBody (
    private val url: String,
    private val internalProgressListener: InternalProgressListener,
    private val responseBody: ResponseBody
) : ResponseBody() {

    companion object {
        // MainLopper 用于在UI线程中执行...
        private var mHandler: Handler = Handler(Looper.getMainLooper())
    }

    interface InternalProgressListener { fun onProgress(url: String, bytesRead: Long, totalBytes: Long) }


    private lateinit var mBufferedSource: BufferedSource

    override fun contentType(): MediaType? {
        return responseBody.contentType()
    }

    override fun contentLength(): Long {
        return responseBody.contentLength()
    }

    override fun source(): BufferedSource {
        mBufferedSource = source(responseBody.source()).buffer()
        return mBufferedSource
    }

    private fun source(source: Source): Source {
        return object : ForwardingSource(source) {

            var mTotalBytesRead: Long = 0
            var mLastTotalBytesRead: Long = 0

            override fun read(sink: Buffer, byteCount: Long): Long {
                val bytesRead = super.read(sink, byteCount)
                mTotalBytesRead += if (bytesRead == -1L) 0 else bytesRead
                if (mLastTotalBytesRead != mTotalBytesRead) {
                    mLastTotalBytesRead = mTotalBytesRead
                    mHandler.post { internalProgressListener.onProgress(url, mTotalBytesRead, contentLength()) }
                }
                return bytesRead
            }
        }
    }
}