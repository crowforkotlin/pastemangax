package com.crow.module_book.ui.adapter.comic

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.app.app
import com.crow.mangax.copymanga.entity.AppConfig.Companion.mDarkMode
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.ui.view.TooltipsView
import com.crow.mangax.copymanga.tryConvert
import com.crow.module_book.R
import com.crow.mangax.R as mangaR
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
    private val mLifecycleScope: LifecycleCoroutineScope,
    private var mClick: (ComicChapterResult) -> Unit
) : RecyclerView.Adapter<ComicChapterRvAdapter.ChapterVH>() {

    inner class ChapterVH(binding: BookFragmentChapterRvBinding) : RecyclerView.ViewHolder(binding.root) {

        private val mButton = binding.button

        init {
            mButton.doOnClickInterval { mClick(mComic[absoluteAdapterPosition]) }
            mButton.doOnLayout { TooltipsView.showTipsWhenLongClick(mButton, it.measuredWidth shr 2) }
        }

        fun onBind(comic: ComicChapterResult) {
            mLifecycleScope.tryConvert(comic.name, mButton::setText)
            if (mChapterName != null && comic.name == mChapterName!!) {
                mButton.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.book_blue))
                mButton.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.white))
            } else {
                mButton.background.setTint(mBtSurfaceColor)
                mButton.setTextColor(mBtTextColor)
            }
        }
    }

    var mChapterName: String? = null

    private var mComic: MutableList<ComicChapterResult> = mutableListOf()

    private var mBtSurfaceColor by Delegates.notNull<Int>()

    private var mBtTextColor by Delegates.notNull<Int>()

    private val mMutex = Mutex()

    init {
        if (mDarkMode) {
            mBtSurfaceColor = ContextCompat.getColor(app, com.google.android.material.R.color.m3_sys_color_dark_surface)
            mBtTextColor = ContextCompat.getColor(app, R.color.book_button_bg_white)
        } else {
            mBtSurfaceColor = ContextCompat.getColor(app, R.color.book_button_bg_white)
            mBtTextColor = ContextCompat.getColor(app, mangaR.color.mangax_black)
        }
    }

    fun getItem(position: Int) = mComic[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterVH {
        return ChapterVH(BookFragmentChapterRvBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    }

    override fun getItemCount(): Int = mComic.size

    override fun onBindViewHolder(vh: ChapterVH, position: Int) { vh.onBind(getItem(position)) }

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