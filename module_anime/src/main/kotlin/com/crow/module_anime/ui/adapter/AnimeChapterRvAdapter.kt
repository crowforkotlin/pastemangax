package com.crow.module_anime.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.ui.adapter.BaseGlideLoadingViewHolder
import com.crow.module_anime.databinding.AnimeFragmentChapterRvBinding
import com.crow.module_anime.model.resp.chapter.AnimeChapterResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_comic/src/main/kotlin/com/crow/module_comic/ui/adapter
 * @Time: 2023/3/15 16:42
 * @Author: CrowForKotlin
 * @Description: ComicInfoChapterRvAdapter
 * @formatter:on
 **************************/

class AnimeChapterRvAdapter(
    private var mClick: (position: Int, chapterResult: AnimeChapterResult) -> Unit
) : RecyclerView.Adapter<AnimeChapterRvAdapter.ChapterVH>() {

    inner class ChapterVH(binding: AnimeFragmentChapterRvBinding) : BaseGlideLoadingViewHolder<AnimeFragmentChapterRvBinding>(binding) {
        
        init {
            binding.play.doOnClickInterval { mClick(absoluteAdapterPosition, getItem(absoluteAdapterPosition)) }
        }
        
        fun onBind(item: AnimeChapterResult) {
            binding.play.text = item.mName
        }
    }

    var mChapterName: String? = null

    private var mChapters: MutableList<AnimeChapterResult> = mutableListOf()

    private var mChaptersUUID : MutableList<String> = mutableListOf()

    private val mMutex = Mutex()

    fun getItem(position: Int) = mChapters[position]

    fun getUUIDS() = mChaptersUUID

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterVH {
        return ChapterVH(AnimeFragmentChapterRvBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int = mChapters.size

    override fun onBindViewHolder(vh: ChapterVH, position: Int) {
        vh.onBind(getItem(position))
    }

    suspend fun doNotify(newDataResult: MutableList<AnimeChapterResult>, delayMs: Long = 1L) {
        mMutex.withLock {
            val isCountSame = itemCount == newDataResult.size
            if (isCountSame) {
                mChaptersUUID = mChapters.map { it.mUUID }.toMutableList()
                mChapters = newDataResult
            }
            else if(itemCount != 0) {
                notifyItemRangeRemoved(0, itemCount)
                mChaptersUUID.clear()
                mChapters.clear()
                delay(BASE_ANIM_200L)
            }
            newDataResult.forEachIndexed { index, data ->
                if (!isCountSame) {
                    mChaptersUUID.add(data.mUUID)
                    mChapters.add(data)
                    notifyItemInserted(index)
                } else notifyItemChanged(index)
                delay(delayMs)
            }
        }
    }
}