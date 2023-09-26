package com.crow.module_book.ui.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.app.appContext
import com.crow.base.copymanga.appIsDarkMode
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.ui.view.ToolTipsView
import com.crow.module_book.R
import com.crow.module_book.databinding.BookFragmentChapterRvBinding
import com.crow.module_book.model.resp.novel_chapter.NovelChapterResult
import kotlinx.coroutines.delay
import kotlin.properties.Delegates

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_comic/src/main/kotlin/com/crow/module_comic/ui/adapter
 * @Time: 2023/3/15 16:42
 * @Author: CrowForKotlin
 * @Description: ComicInfoChapterRvAdapter
 * @formatter:on
 **************************/

class NovelChapterRvAdapter(
    private var mComic: MutableList<NovelChapterResult> = mutableListOf(),
    private var mDoOnTapChapter: (NovelChapterResult) -> Unit
) : RecyclerView.Adapter<NovelChapterRvAdapter.ViewHolder>() {

    var mChapterName: String? = null

    private var mBtSurfaceColor by Delegates.notNull<Int>()
    private var mBtTextColor by Delegates.notNull<Int>()

    init {
        if (appIsDarkMode) {
            mBtSurfaceColor = ContextCompat.getColor(appContext, com.google.android.material.R.color.m3_sys_color_dark_surface)
            mBtTextColor = ContextCompat.getColor(appContext, R.color.book_button_bg_white)
        } else {
            mBtSurfaceColor = ContextCompat.getColor(appContext, R.color.book_button_bg_white)
            mBtTextColor = ContextCompat.getColor(appContext, R.color.book_button_text_purple)
        }
    }

    inner class ViewHolder(rvBinding: BookFragmentChapterRvBinding) : RecyclerView.ViewHolder(rvBinding.root) { val mButton = rvBinding.comicInfoRvChip }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(BookFragmentChapterRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)).also { vh ->
            vh.mButton.doOnClickInterval { mDoOnTapChapter(mComic[vh.absoluteAdapterPosition]) }
        }
    }

    override fun getItemCount(): Int = mComic.size


    override fun onBindViewHolder(vh: ViewHolder, position: Int) {

        val comic = mComic[position]
        vh.mButton.text = comic.name
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) vh.mButton.tooltipText = comic.name
        else ToolTipsView.showToolTipsByLongClick(vh.mButton)
        if (mChapterName != null && comic.name == mChapterName!!) {
            vh.mButton.setBackgroundColor(ContextCompat.getColor(vh.itemView.context, R.color.book_blue))
            vh.mButton.setTextColor(ContextCompat.getColor(vh.itemView.context, android.R.color.white))
        } else {
            vh.mButton.background.setTint(mBtSurfaceColor)
            vh.mButton.setTextColor(mBtTextColor)
        }
    }

    suspend fun doNotify(newDataResult: MutableList<NovelChapterResult>, delayMs: Long = 1L) {
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