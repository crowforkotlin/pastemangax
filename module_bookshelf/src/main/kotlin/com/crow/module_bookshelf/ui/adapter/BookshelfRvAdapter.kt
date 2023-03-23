package com.crow.module_bookshelf.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.crow.base.current_project.getComicCardHeight
import com.crow.base.current_project.getComicCardWidth
import com.crow.module_bookshelf.databinding.BookshelfFragmentRvBinding
import com.crow.module_bookshelf.model.resp.book_shelf.BookshelfResults

class BookshelfRvAdapter(private val clickCallback: (BookshelfResults) -> Unit) : PagingDataAdapter<BookshelfResults, BookshelfRvAdapter.ViewHolder>(DiffCallback()) {

    class DiffCallback: DiffUtil.ItemCallback<BookshelfResults>() {
        override fun areItemsTheSame(oldItem: BookshelfResults, newItem: BookshelfResults): Boolean {
            return oldItem.mUuid == newItem.mUuid
        }

        override fun areContentsTheSame(oldItem: BookshelfResults, newItem: BookshelfResults): Boolean {
            return oldItem == newItem
        }
    }

    inner class ViewHolder(val rvBinding: BookshelfFragmentRvBinding): RecyclerView.ViewHolder(rvBinding.root) {


    }

    override fun onBindViewHolder(vh: ViewHolder, position: Int) {
        val item = getItem(position) ?: return
        Glide.with(vh.itemView.context).load(item.mComic.mCover).into(vh.rvBinding.bookshelfRvImage)
        vh.rvBinding.bookshelfRvName.text = item.mComic.mName
        vh.rvBinding.bookshelfRvTime.text = item.mComic.mDatetimeUpdated
    }

    // 父布局高度
    private var mParentHeight: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(BookshelfFragmentRvBinding.inflate(LayoutInflater.from(parent.context), parent,false)).also { vh ->

            vh.rvBinding.bookshelfRvImage.layoutParams.apply {
                width = getComicCardWidth()
                height = getComicCardHeight()
            }
        }
    }
}