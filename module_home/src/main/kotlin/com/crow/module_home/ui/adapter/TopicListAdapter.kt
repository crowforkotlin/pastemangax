package com.crow.module_home.ui.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.request.transition.DrawableCrossFadeTransition
import com.bumptech.glide.request.transition.NoTransition
import com.crow.base.R
import com.crow.base.app.app
import com.crow.base.copymanga.appComicCardHeight
import com.crow.base.copymanga.appComicCardWidth
import com.crow.base.copymanga.formatValue
import com.crow.base.copymanga.glide.AppGlideProgressFactory
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.tools.extensions.px2sp
import com.crow.base.ui.adapter.BaseGlideLoadingViewHolder
import com.crow.module_home.databinding.HomeTopicRvBinding
import com.crow.module_home.model.resp.topic.TopicResult
import com.google.android.material.chip.Chip

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Package: com.crow.module_main.ui.adapter
 * @Time: 2023/10/3 18:39
 * @Author: CrowForKotlin
 * @Description: HistoryListAdapter
 * @formatter:on
 **************************/
class TopicListAdapter(private val onClick: (name: String, pathword: String) -> Unit) :
    PagingDataAdapter<TopicResult, TopicListAdapter.HistoryVH>(DiffCallback()) {

    private val mChipTextSize = app.px2sp(app.resources.getDimension(R.dimen.base_sp12_5))

    inner class HistoryVH(binding: HomeTopicRvBinding) : BaseGlideLoadingViewHolder<HomeTopicRvBinding>(binding) {

        init {
            binding.card.layoutParams.apply {
                width = appComicCardWidth
                height = appComicCardHeight
            }

            itemView.doOnClickInterval { (getItem(absoluteAdapterPosition) ?: return@doOnClickInterval).apply { onClick(mName, mPathWord) } }
        }

        fun onBind(item: TopicResult) {

            binding.loading.isVisible = true

            binding.loadingText.isVisible = true

            binding.loadingText.text = AppGlideProgressFactory.PERCENT_0

            mAppGlideProgressFactory?.onRemoveListener()?.onCleanCache()

            mAppGlideProgressFactory = AppGlideProgressFactory.createGlideProgressListener(item.mCover) { _, _, percentage, _, _ -> binding.loadingText.text = AppGlideProgressFactory.getProgressString(percentage) }

            Glide.with(itemView)
                .load(item.mCover)
                .addListener(mAppGlideProgressFactory?.getRequestListener())
                .transition(GenericTransitionOptions<Drawable>().transition { dataSource, _ ->
                    if (dataSource == DataSource.REMOTE) {
                        binding.loading.isInvisible = true
                        binding.loadingText.isInvisible = true
                        DrawableCrossFadeTransition(BASE_ANIM_200L.toInt(), true)
                    } else {
                        binding.loading.isInvisible = true
                        binding.loadingText.isInvisible = true
                        NoTransition()
                    }
                })
                .into(binding.image)


            binding.chipGroup.removeAllViews()
            item?.mTheme?.forEach {
                val chip = Chip(itemView.context)
                chip.text = it.mName
                chip.textSize = mChipTextSize
                binding.chipGroup.addView(chip)
            }

            binding.name.text = item.mName
            binding.author.text = item.mAuthor.joinToString { it.mName }
            binding.hot.text = formatValue(item.mPopular)
        }
    }

    /**
     * ● DiffCallback
     *
     * ● 2023-11-01 00:05:03 周三 上午
     * @author crowforkotlin
     */
    class DiffCallback : DiffUtil.ItemCallback<TopicResult>() {
        override fun areItemsTheSame(oldItem: TopicResult, newItem: TopicResult): Boolean {
            return oldItem.mPathWord == newItem.mPathWord
        }

        override fun areContentsTheSame(oldItem: TopicResult, newItem: TopicResult): Boolean {
            return oldItem == newItem
        }
    }

    /**
     * ● 复用VH
     *
     * ● 2023-11-01 00:04:50 周三 上午
     * @author crowforkotlin
     */
    override fun onBindViewHolder(holder: HistoryVH, position: Int) { holder.onBind(getItem(position) ?: return) }

    /**
     * ● 创建VH
     *
     * ● 2023-11-01 00:04:43 周三 上午
     * @author crowforkotlin
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryVH { return HistoryVH(HomeTopicRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)) }
}