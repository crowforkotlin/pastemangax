package com.crow.base.ui.adapter

import android.view.ViewPropertyAnimator
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.crow.base.copymanga.glide.AppGlideProgressFactory

open class BaseGlideLoadingViewHolder<VB: ViewBinding>(val rvBinding: VB) : RecyclerView.ViewHolder(rvBinding.root) {
    var mAppGlideProgressFactory: AppGlideProgressFactory? = null
    var mLoadingPropertyAnimator: ViewPropertyAnimator? = null
    var mTextPropertyAnimator: ViewPropertyAnimator? = null
}