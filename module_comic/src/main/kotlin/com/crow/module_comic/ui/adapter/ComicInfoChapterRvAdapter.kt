package com.crow.module_comic.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.tools.extensions.clickGap
import com.crow.module_comic.R
import com.crow.module_comic.databinding.ComicInfoRvChapterBinding
import com.crow.module_comic.model.resp.comic_chapter.Comic
import kotlinx.coroutines.delay

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_comic/src/main/kotlin/com/crow/module_comic/ui/adapter
 * @Time: 2023/3/15 16:42
 * @Author: CrowForKotlin
 * @Description: ComicInfoChapterRvAdapter
 * @formatter:on
 **************************/
class ComicInfoChapterRvAdapter(private var mComic: MutableList<Comic> = mutableListOf()) : RecyclerView.Adapter<ComicInfoChapterRvAdapter.ViewHolder>() {

    fun interface ChipCLickCallBack {
        fun onClick(mComic: Comic)
    }

    var mChapterName: String? = null

    private var mChipCLickCallBack: ChipCLickCallBack? = null
    private var mClickFlag = false
    private var mDelayMs: Long = 20L

    inner class ViewHolder(rvBinding: ComicInfoRvChapterBinding) : RecyclerView.ViewHolder(rvBinding.root) {
        val mChip = rvBinding.comicInfoRvChip
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ComicInfoRvChapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)).also { vh ->
            vh.mChip.clickGap { _, _ ->
                if (mClickFlag) return@clickGap
                mClickFlag = true
                mChipCLickCallBack?.onClick(mComic[vh.absoluteAdapterPosition])
            }
        }
    }

    override fun getItemCount(): Int = mComic.size

    override fun onBindViewHolder(vh: ViewHolder, position: Int) {
        val comic = mComic[position]
        vh.mChip.text = comic.name
        if (mChapterName != null) {
            if (comic.name == mChapterName!!) {
                vh.mChip.chipBackgroundColor = ContextCompat.getColorStateList(vh.itemView.context, R.color.comic_blue)
                vh.mChip.setTextColor(ContextCompat.getColor(vh.itemView.context, android.R.color.white))
                mChapterName = null
            }
        }
    }

    suspend fun doNotify(datas: MutableList<Comic>, delayMs: Long = mDelayMs) {
        val itemSize = itemCount
        val isSizeSame = itemSize == datas.size
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

    fun getDataSize() = mComic.size

    fun addListener(chipCLickCallBack: ChipCLickCallBack) { mChipCLickCallBack = chipCLickCallBack }
    fun setData(list: List<Comic>) { mComic = list.toMutableList() }
}