package com.crow.mangax.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.crow.mangax.copymanga.okhttp.AppProgressFactory

open class BaseGlideLoadingViewHolder<VB: ViewBinding>(val binding: VB) : RecyclerView.ViewHolder(binding.root) {
    var mAppGlideProgressFactory: AppProgressFactory? = null
}