package com.crow.module_comic.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.tools.extensions.clickGap
import com.crow.module_comic.databinding.ComicInfoRvChapterBinding
import com.crow.module_comic.model.resp.comic_chapter.Comic
import com.crow.module_comic.model.resp.ChapterResultsResp

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_comic/src/main/kotlin/com/crow/module_comic/ui/adapter
 * @Time: 2023/3/15 16:42
 * @Author: CrowForKotlin
 * @Description: ComicInfoChapterRvAdapter
 * @formatter:on
 **************************/
class ComicInfoChapterRvAdapter(private var mComic: ChapterResultsResp? = null) : RecyclerView.Adapter<ComicInfoChapterRvAdapter.ViewHolder>() {

    fun interface ChipCLickCallBack {
        fun onClick(mComic: Comic)
    }

    private var mChipCLickCallBack: ChipCLickCallBack? = null
    private var mClickFlag = false

    inner class ViewHolder(rvBinding: ComicInfoRvChapterBinding) : RecyclerView.ViewHolder(rvBinding.root) {
        val mChip = rvBinding.comicInfoRvChip
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ComicInfoRvChapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)).also {
            it.mChip.clickGap { _, _ ->
                if (mClickFlag) return@clickGap
                mClickFlag = true
                mChipCLickCallBack?.onClick((mComic ?: return@clickGap).list[it.absoluteAdapterPosition])
            }
        }
    }

    override fun getItemCount(): Int {
        val total = mComic?.total ?: 0
        val limit = mComic?.limit ?: 0
        return if (total > limit) limit else total
    }

    override fun onBindViewHolder(vh: ViewHolder, position: Int) {
        val comic = (mComic ?: return).list[position]
        vh.mChip.text = comic.name
    }

    fun setData(data: ChapterResultsResp) { mComic = data }

    fun getDataSize() = mComic?.total ?: 0

    fun addListener(chipCLickCallBack: ChipCLickCallBack) { mChipCLickCallBack = chipCLickCallBack }
}