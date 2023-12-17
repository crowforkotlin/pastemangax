package com.crow.module_book.ui.adapter.comic

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.app.app
import com.crow.mangax.copymanga.appIsDarkMode
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.ui.view.TooltipsView
import com.crow.module_book.R
import com.crow.module_book.databinding.BookFragmentChapterRvBinding
import com.crow.module_book.model.resp.comic_chapter.ComicChapterResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.properties.Delegates

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_comic/src/main/kotlin/com/crow/module_comic/ui/adapter
 * @Time: 2023/3/15 16:42
 * @Author: CrowForKotlin
 * @Description: ComicInfoChapterRvAdapter
 * @formatter:on
 **************************/

class ComicChapterRvAdapter(
    private var mDoOnTapChapter: (ComicChapterResult) -> Unit
) : RecyclerView.Adapter<ComicChapterRvAdapter.ChapterVH>() {

    inner class ChapterVH(rvBinding: BookFragmentChapterRvBinding) : RecyclerView.ViewHolder(rvBinding.root) { val mButton = rvBinding.comicInfoRvChip }

    var mChapterName: String? = null

    private var mComic: MutableList<ComicChapterResult> = mutableListOf()

    private var mBtSurfaceColor by Delegates.notNull<Int>()

    private var mBtTextColor by Delegates.notNull<Int>()

    private val mMutex = Mutex()

    init {
        if (appIsDarkMode) {
            mBtSurfaceColor = ContextCompat.getColor(app, com.google.android.material.R.color.m3_sys_color_dark_surface)
            mBtTextColor = ContextCompat.getColor(app, R.color.book_button_bg_white)
        } else {
            mBtSurfaceColor = ContextCompat.getColor(app, R.color.book_button_bg_white)
            mBtTextColor = ContextCompat.getColor(app, com.crow.base.R.color.base_black)
        }
    }

    fun getItem(position: Int) = mComic[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterVH {
        return ChapterVH(BookFragmentChapterRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)).also { vh ->
            vh.mButton.doOnClickInterval { mDoOnTapChapter(mComic[vh.absoluteAdapterPosition]) }
            vh.mButton.doOnLayout {
                TooltipsView.showTipsWhenLongClick(vh.mButton, it.measuredWidth shr 2)
            }
        }
    }

    override fun getItemCount(): Int = mComic.size

    override fun onBindViewHolder(vh: ChapterVH, position: Int) {
        val comic = getItem(position)
        vh.mButton.text = comic.name
        if (mChapterName != null && comic.name == mChapterName!!) {
            vh.mButton.setBackgroundColor(ContextCompat.getColor(vh.itemView.context, R.color.book_blue))
            vh.mButton.setTextColor(ContextCompat.getColor(vh.itemView.context, android.R.color.white))
        } else {
            vh.mButton.background.setTint(mBtSurfaceColor)
            vh.mButton.setTextColor(mBtTextColor)
        }
    }

    suspend fun doNotify(newDataResult: MutableList<ComicChapterResult>, delayMs: Long = 1L) {
        mMutex.withLock {
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

}