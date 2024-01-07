package com.crow.module_bookshelf.ui.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.request.transition.DrawableCrossFadeTransition
import com.crow.mangax.copymanga.appComicCardHeight
import com.crow.mangax.copymanga.appComicCardWidth
import com.crow.mangax.copymanga.appDp10
import com.crow.mangax.copymanga.okhttp.AppProgressFactory
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.mangax.copymanga.entity.AppConfigEntity.Companion.mChineseConvert
import com.crow.mangax.copymanga.tryConvert
import com.crow.mangax.tools.language.ChineseConverter
import com.crow.mangax.ui.adapter.MangaCoilVH
import com.crow.module_bookshelf.databinding.BookshelfFragmentRvBinding
import com.crow.module_bookshelf.model.resp.bookshelf_novel.BookshelfNovelResults
import kotlinx.coroutines.launch

class BSNovelRvAdapter(
    private val mLifecycleScope: LifecycleCoroutineScope,
    private val onClick: (BookshelfNovelResults) -> Unit
) : PagingDataAdapter<BookshelfNovelResults, BSNovelRvAdapter.BSViewHolder>(DiffCallback()) {

    /**
     * ● DiffCallback
     *
     * ● 2023-10-22 01:34:54 周日 上午
     * @author crowforkotlin
     */
    class DiffCallback: DiffUtil.ItemCallback<BookshelfNovelResults>() {
        override fun areItemsTheSame(oldItem: BookshelfNovelResults, newItem: BookshelfNovelResults): Boolean {
            return oldItem.mUuid == newItem.mUuid
        }

        override fun areContentsTheSame(oldItem: BookshelfNovelResults, newItem: BookshelfNovelResults): Boolean {
            return oldItem == newItem
        }
    }

    /**
     * ● Bookshelf Novel ViewHolder
     *
     * ● 2023-10-22 01:35:08 周日 上午
     * @author crowforkotlin
     */
    inner class BSViewHolder(binding: BookshelfFragmentRvBinding) : MangaCoilVH<BookshelfFragmentRvBinding>(binding) {

        init {
            initComponent(binding.loading, binding.loadingText, binding.image)
            binding.image.layoutParams.height = appComicCardHeight
            binding.image.doOnClickInterval { onClick(getItem(absoluteAdapterPosition) ?: return@doOnClickInterval) }
        }
        
        fun onBind(item: BookshelfNovelResults) {
            binding.time.text = item.mNovel.mDatetimeUpdated
            mLifecycleScope.tryConvert(item.mNovel.mName, binding.name::setText)
            loadImage(item.mNovel.mCover)
        }
    }

    /**
     * ● onCreateVH
     *
     * ● 2023-10-22 01:35:24 周日 上午
     * @author crowforkotlin
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = BSViewHolder(BookshelfFragmentRvBinding.inflate(LayoutInflater.from(parent.context), parent,false))

    /**
     * ● onBindVH
     *
     * ● 2023-10-22 01:35:34 周日 上午
     * @author crowforkotlin
     */
    override fun onBindViewHolder(vh: BSViewHolder, position: Int) { vh.onBind(getItem(position) ?: return) }
}