package com.crow.module_comic.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.crow.module_book.databinding.BookComicRvBinding
import com.crow.module_comic.model.resp.ComicPageResp

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_comic/src/main/kotlin/com/crow/module_comic/ui/adapter
 * @Time: 2023/3/15 16:42
 * @Author: CrowForKotlin
 * @Description: ComicInfoChapterRvAdapter
 * @formatter:on
 **************************/
class ComicRvAdapter(private var mComicPageResp: ComicPageResp? = null) : RecyclerView.Adapter<ComicRvAdapter.ViewHolder>() {

    inner class ViewHolder(rvBinding: BookComicRvBinding) : RecyclerView.ViewHolder(rvBinding.root) {
        val mPhotoView = rvBinding.comicRvPhotoview
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(BookComicRvBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int = (mComicPageResp?.mChapter?.mSize) ?: 0

    override fun onBindViewHolder(vh: ViewHolder, position: Int) {
        val chapter = (mComicPageResp ?: return).mChapter
        val content = chapter.mContents[position]
        Glide.with(vh.itemView).load(content.url).into(vh.mPhotoView)
    }

    fun setData(data: ComicPageResp) { mComicPageResp = data }

    fun getDataSize() = (mComicPageResp?.mChapter?.mSize) ?: 0
}