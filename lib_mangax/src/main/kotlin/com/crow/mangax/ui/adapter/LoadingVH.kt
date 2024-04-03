package com.crow.mangax.ui.adapter

import androidx.viewbinding.ViewBinding
import com.crow.base.ui.recyclerView.BaseViewHolder
import com.crow.mangax.copymanga.okhttp.AppProgressFactory

/**
 * ● LoadingVH
 *
 * ● 2024/3/28 23:04
 * @author crowforkotlin
 * @formatter:on
 */
open class LoadingVH<VB: ViewBinding>(loadingVB: VB) : BaseViewHolder<VB>(loadingVB) {

    var mLoadingFactory: AppProgressFactory? = null

    inline fun loading(link: String, crossinline onProgress: (String) -> Unit) {

        mLoadingFactory
            ?.removeProgressListener()
            ?.remove()

        mLoadingFactory = AppProgressFactory.createProgressListener(link) { _, _, percentage, _, _ -> onProgress(AppProgressFactory.formateProgress(percentage)) }
    }
}