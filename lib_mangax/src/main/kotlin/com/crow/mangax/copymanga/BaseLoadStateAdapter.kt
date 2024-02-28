package com.crow.mangax.copymanga

import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.databinding.BasePagingFooterRetryBinding
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.animateFadeOut
import com.crow.base.tools.extensions.animateFadeOutGone
import com.crow.base.tools.extensions.doOnClickInterval
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BaseLoadStateAdapter(private val doOnRetry: () -> Unit) : LoadStateAdapter<BaseLoadStateAdapter.LoadStateViewHolder>() {


    inner class LoadStateViewHolder(val binding: BasePagingFooterRetryBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(loadState: LoadState) {
            binding.loading.isVisible = loadState is LoadState.Loading
            binding.retry.isVisible = loadState is LoadState.Error
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadStateViewHolder {
        return LoadStateViewHolder(BasePagingFooterRetryBinding.inflate(LayoutInflater.from(parent.context), parent, false)).also { vh ->
            vh.binding.root.layoutParams.height = appComicCardHeight / 2
            vh.binding.retry.doOnClickInterval { doOnRetry() }
        }
    }

    override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState, ) = holder.bind(loadState)
}