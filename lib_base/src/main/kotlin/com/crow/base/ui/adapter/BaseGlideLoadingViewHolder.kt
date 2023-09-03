package com.crow.base.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.crow.base.copymanga.glide.AppGlideProgressFactory

open class BaseGlideLoadingViewHolder<VB: ViewBinding>(val binding: VB) : RecyclerView.ViewHolder(binding.root) {
    var mAppGlideProgressFactory: AppGlideProgressFactory? = null
}