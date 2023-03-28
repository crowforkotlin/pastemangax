package com.crow.module_comic.ui.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.clickGap
import com.crow.module_book.R
import com.crow.module_book.databinding.BookComicInfoRvChapterBinding
import com.crow.module_comic.model.resp.comic_chapter.ComicChapterResult
import kotlinx.coroutines.delay

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_comic/src/main/kotlin/com/crow/module_comic/ui/adapter
 * @Time: 2023/3/15 16:42
 * @Author: CrowForKotlin
 * @Description: ComicInfoChapterRvAdapter
 * @formatter:on
 **************************/

/*
class ComicChapterRvAdapter(
    inline var mDoOnChapterTap: (ComicChapterResult) -> Unit
) : ListAdapter<ComicChapterResult, ComicChapterRvAdapter.ViewHolder>(DiffCallback()) {

    class DiffCallback() : DiffUtil.ItemCallback<ComicChapterResult>() {
        override fun areItemsTheSame(oldItem: ComicChapterResult, newItem: ComicChapterResult): Boolean {
            return oldItem.uuid == newItem.uuid
        }

        override fun areContentsTheSame(oldItem: ComicChapterResult, newItem: ComicChapterResult): Boolean {
            return oldItem == newItem
        }
    }

    var mChapterName: String? = null

    private var mClickFlag = false
    private var mDelayMs: Long = 1L

    inner class ViewHolder(rvBinding: BookComicInfoRvChapterBinding) : RecyclerView.ViewHolder(rvBinding.root) {
        val mButton = rvBinding.comicInfoRvChip
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(BookComicInfoRvChapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)).also { vh ->
            vh.mButton.clickGap { _, _ ->
                if (mClickFlag) return@clickGap
                mClickFlag = true
                mDoOnChapterTap(getItem(vh.absoluteAdapterPosition))
            }
        }
    }

    override fun onBindViewHolder(vh: ViewHolder, position: Int) {
        val comic = getItem(position)
        vh.mButton.text = comic.name
        if (mChapterName != null) {
            if (comic.name == mChapterName!!) {
                vh.mButton.setBackgroundColor(ContextCompat.getColor(vh.itemView.context, R.color.comic_blue))
                vh.mButton.setTextColor(ContextCompat.getColor(vh.itemView.context, android.R.color.white))
                mChapterName = null
            }
        }
    }

*/
/*    suspend fun doNotify(datas: MutableList<ComicChapterResult>, delayMs: Long = mDelayMs) {
        val itemSize = itemCount
        itemSize.logMsg()
        val isSizeSame = itemSize == datas.size
        isSizeSame.logMsg()
        if (isSizeSame) mComic = datas
        else if(itemSize != 0) {
            notifyItemRangeRemoved(0, itemSize)
            mComic.clear()
        }
        datas.forEachIndexed { index, data ->
            if (!isSizeSame) {
                mComic.add(data)
                notifyItemInserted(index)
            } else notifyItemChanged(index)
            delay(delayMs)
        }
    }

    fun doNotify(datas: MutableList<ComicChapterResult>) {
        mComic = datas
        if (mComic.isEmpty()) {
            mComic = datas
            notifyItemRangeInserted(0, itemCount)
            return
        }
        val oldItemCount = itemCount
        mComic = datas
        if (itemCount < oldItemCount) notifyItemRangeRemoved(itemCount, oldItemCount)
        notifyItemRangeChanged(0, itemCount)
    }

    fun getDataSize() = mComic.size*//*

}
*/

class ComicChapterRvAdapter(
    private var mComic: MutableList<ComicChapterResult> = mutableListOf(),
    private var mDoOnTapChapter: (ComicChapterResult) -> Unit
) : RecyclerView.Adapter<ComicChapterRvAdapter.ViewHolder>() {

    var mChapterName: String? = null

    private var mClickFlag = false
    private var mSurfaceBtColor: ColorStateList? = null
    private var mTextBtColor: ColorStateList? = null

    inner class ViewHolder(rvBinding: BookComicInfoRvChapterBinding) : RecyclerView.ViewHolder(rvBinding.root) { val mButton = rvBinding.comicInfoRvChip }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(BookComicInfoRvChapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)).also { vh ->
            vh.mButton.clickGap { _, _ ->
                if (mClickFlag) return@clickGap
                mClickFlag = true
                mDoOnTapChapter(mComic[vh.absoluteAdapterPosition])
            }
        }
    }

    override fun getItemCount(): Int = mComic.size

    override fun getItemViewType(position: Int): Int = position

    override fun onBindViewHolder(vh: ViewHolder, position: Int) {

        val comic = mComic[position]
        vh.mButton.text = comic.name
        if (mChapterName != null && comic.name == mChapterName!!) {
            vh.mButton.setBackgroundColor(ContextCompat.getColor(vh.itemView.context, R.color.comic_blue))
            vh.mButton.setTextColor(ContextCompat.getColor(vh.itemView.context, android.R.color.white))
            return
        }

        if (mSurfaceBtColor == null || mTextBtColor == null) {
            mSurfaceBtColor = vh.mButton.backgroundTintList
            mTextBtColor = vh.mButton.textColors
        }
        vh.mButton.background.setTintList(mSurfaceBtColor)
        vh.mButton.setTextColor(mTextBtColor)
    }

    suspend fun doNotify(newDataResult: MutableList<ComicChapterResult>, delayMs: Long = 1L) {
        val isCountSame = itemCount == newDataResult.size
        if (isCountSame) mComic = newDataResult
        else if(itemCount != 0) {
            notifyItemRangeRemoved(0, itemCount)
            mComic.clear()
            delay(BASE_ANIM_200L)
        }
        newDataResult.forEachIndexed { index, data ->
            if (!isCountSame) {
                mComic.add(data)
                notifyItemInserted(index)
            } else notifyItemChanged(index)
            delay(delayMs)
        }
    }
}