package com.crow.base.services.download

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/java/com/crow/base/services/download
 * @Time: 2023/3/3 21:52
 * @Author: BarryAllen
 * @Description: DownLoadListener
 * @formatter:on
 **************************/
interface DownLoadListener {

    fun onProgress(progress: Int)
    fun onSuccess()
    fun onFailure()
    fun onPaused()
    fun onCanceled()
}