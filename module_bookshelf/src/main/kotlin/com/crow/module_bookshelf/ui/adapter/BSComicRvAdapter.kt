package com.crow.module_bookshelf.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.crow.mangax.copymanga.MangaXAccountConfig
import com.crow.mangax.copymanga.appComicCardHeight
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.mangax.copymanga.entity.CatlogConfig.mChineseConvert
import com.crow.mangax.tools.language.ChineseConverter
import com.crow.mangax.ui.adapter.MangaCoilVH
import com.crow.module_bookshelf.databinding.BookshelfFragmentRvBinding
import com.crow.module_bookshelf.model.resp.bookshelf_comic.BookshelfComicResults
import kotlinx.coroutines.launch

class BSComicRvAdapter(
    private val mLifecycleScope: LifecycleCoroutineScope,
    private val onClick: (BookshelfComicResults) -> Unit
) : PagingDataAdapter<BookshelfComicResults, BSComicRvAdapter.BSViewHolder>(DiffCallback()) {

    /**
     * ⦁ DiffCallback
     *
     * ⦁ 2023-10-22 01:28:53 周日 上午
     * @author crowforkotlin
     */
    class DiffCallback: DiffUtil.ItemCallback<BookshelfComicResults>() {
        override fun areItemsTheSame(oldItem: BookshelfComicResults, newItem: BookshelfComicResults): Boolean {
            return oldItem.mUuid == newItem.mUuid
        }

        override fun areContentsTheSame(oldItem: BookshelfComicResults, newItem: BookshelfComicResults): Boolean {
            return oldItem == newItem
        }
    }

    /**
     * ⦁ Bookshelf ViewHolder
     *
     * ⦁ 2023-10-22 01:29:27 周日 上午
     * @author crowforkotlin
     */
    inner class BSViewHolder(binding: BookshelfFragmentRvBinding) : MangaCoilVH<BookshelfFragmentRvBinding>(binding) {

        init {
            initComponent(binding.loading, binding.loadingText, binding.image)
            binding.image.layoutParams.height = appComicCardHeight
            binding.image.doOnClickInterval { onClick(getItem(absoluteAdapterPosition) ?: return@doOnClickInterval) }
        }

        fun onBind(item: BookshelfComicResults) {
            if (MangaXAccountConfig.mAccountToken.isNotEmpty()) {
                binding.imageNew.isInvisible = (item.mLastBrowse?.mLastBrowseName == item.mComic.mLastChapterName)
            } else {
                binding.imageNew.isGone = true
            }
            loadCoverImage(item.mComic.mCover)
            mLifecycleScope.launch { binding.name.text = if (mChineseConvert) ChineseConverter.convert(item.mComic.mName) else item.mComic.mName }
            binding.time.text = item.mComic.mDatetimeUpdated
        }
    }

    /**
     * ⦁ onCreateVH
     *
     * ⦁ 2023-10-22 01:29:37 周日 上午
     * @author crowforkotlin
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = BSViewHolder(BookshelfFragmentRvBinding.inflate(LayoutInflater.from(parent.context), parent,false))

    /**
     * ⦁ onBindVH
     *
     * ⦁ 2023-10-22 01:29:46 周日 上午
     * @author crowforkotlin
     */
    override fun onBindViewHolder(vh: BSViewHolder, position: Int) { vh.onBind(getItem(position) ?: return) }
}