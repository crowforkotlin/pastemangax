package com.crow.module_discover.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.copymanga.appComicCardHeight
import com.crow.base.databinding.BasePagingFooterRetryBinding
import com.crow.base.tools.extensions.doOnClickInterval

class DiscoverLoadStateAdapter(private val retry: () -> Unit) : LoadStateAdapter<DiscoverLoadStateAdapter.LoadStateViewHolder>() {


    inner class LoadStateViewHolder(val rvBinding: BasePagingFooterRetryBinding) : RecyclerView.ViewHolder(rvBinding.root) {
        fun bind(loadState: LoadState) {
            rvBinding.baseLoadingLottie.isVisible = loadState is LoadState.Loading
            rvBinding.baseLoadingRetry.isVisible = loadState is LoadState.Error
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadStateViewHolder {
        return LoadStateViewHolder(BasePagingFooterRetryBinding.inflate(LayoutInflater.from(parent.context), parent, false)).also {  vh ->
            vh.rvBinding.root.layoutParams.height = appComicCardHeight / 2
            vh.rvBinding.baseLoadingRetry.doOnClickInterval { retry() }
        }
    }

    override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState, ) = holder.bind(loadState)
}