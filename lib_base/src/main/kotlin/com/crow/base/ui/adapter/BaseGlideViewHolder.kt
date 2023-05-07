package com.crow.base.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.crow.base.copymanga.glide.AppGlideProgressFactory

open class BaseGlideViewHolder<VB: ViewBinding>(val rvBinding: VB) : RecyclerView.ViewHolder(rvBinding.root) {
    var mAppGlideProgressFactory: AppGlideProgressFactory? = null
}