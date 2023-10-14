package com.crow.module_anime.ui.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.request.transition.DrawableCrossFadeTransition
import com.bumptech.glide.request.transition.NoTransition
import com.crow.base.copymanga.appComicCardWidth
import com.crow.base.copymanga.glide.AppGlideProgressFactory
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
    private var mClick: (AnimeChapterResult) -> Unit
) : RecyclerView.Adapter<AnimeChapterRvAdapter.ChapterVH>() {

    inner class ChapterVH(binding: AnimeFragmentChapterRvBinding) : BaseGlideLoadingViewHolder<AnimeFragmentChapterRvBinding>(binding) {
        
        init {
            binding.image.layoutParams.height = appComicCardWidth
            binding.image.layoutParams.width = appComicCardWidth

            binding.play.doOnClickInterval { mClick(getItem(absoluteAdapterPosition)) }
        }
        
        fun onBind(item: AnimeChapterResult) {

            binding.loading.isVisible = true
            binding.loadingText.isVisible = true
            binding.loadingText.text = AppGlideProgressFactory.PERCENT_0
            mAppGlideProgressFactory?.doRemoveListener()?.doClean()

            mAppGlideProgressFactory = AppGlideProgressFactory.createGlideProgressListener(item.mVCover) { _, _, percentage, _, _ ->
                binding.loadingText.text = AppGlideProgressFactory.getProgressString(percentage)
            }

            Glide.with(itemView.context)
                .load(item.mVCover)
                .addListener(mAppGlideProgressFactory?.getRequestListener())
                .transition(GenericTransitionOptions<Drawable>().transition { dataSource, _ ->
                    if (dataSource == DataSource.REMOTE) {
                        binding.loading.isInvisible = true
                        binding.loadingText.isInvisible = true
                        DrawableCrossFadeTransition(BASE_ANIM_200L.toInt(), true)
                    } else {
                        binding.loading.isInvisible = true
                        binding.loadingText.isInvisible = true
                        NoTransition()
                    }
                })
                .into(binding.image)

            binding.name.text = item.mName
            binding.line.text = item.mLines.joinToString { it.mName }
        }
    }

    var mChapterName: String? = null

    private var mChapters: MutableList<AnimeChapterResult> = mutableListOf()

    private val mMutex = Mutex()

    fun getItem(position: Int) = mChapters[position]

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
            if (isCountSame) mChapters = newDataResult
            else if(itemCount != 0) {
                notifyItemRangeRemoved(0, itemCount)
                mChapters.clear()
                delay(BASE_ANIM_200L)
            }
            newDataResult.forEachIndexed { index, data ->
                if (!isCountSame) {
                    mChapters.add(data)
                    notifyItemInserted(index)
                } else notifyItemChanged(index)
                delay(delayMs)
            }
        }
    }
}