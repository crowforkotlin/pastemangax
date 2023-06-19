@file:Suppress("UNCHECKED_CAST")

package com.crow.module_home.ui.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater.from
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.doOnLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.request.transition.DrawableCrossFadeTransition
import com.bumptech.glide.request.transition.NoTransition
import com.crow.base.copymanga.*
import com.crow.base.copymanga.entity.IBookAdapterColor
import com.crow.base.copymanga.glide.AppGlideProgressFactory
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.ui.adapter.BaseGlideLoadingViewHolder
import com.crow.base.ui.view.ToolTipsView
import com.crow.module_home.databinding.HomeFragmentComicRvBodyNewBinding
import com.crow.module_home.model.resp.homepage.*
import com.crow.module_home.model.resp.homepage.results.AuthorResult
import com.crow.module_home.model.resp.homepage.results.RecComicsResult
import com.crow.module_home.ui.adapter.HomeComicParentRvAdapter.Type
import kotlinx.coroutines.delay
import java.util.*

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_home/src/main/kotlin/com/crow/module_home/ui/adapter
 * @Time: 2023/3/11 2:17
 * @Author: CrowForKotlin
 * @Description: HomeBookAdapter
 * @formatter:on
 **************************/
class HomeComicChildRvAdapter<T>(
    private var mData: MutableList<T> = mutableListOf(),
    private val mType: Type,
    val doOnTap: (String) -> Unit
) : RecyclerView.Adapter<HomeComicChildRvAdapter<T>.LoadingViewHolder>() , IBookAdapterColor<HomeComicChildRvAdapter<T>.LoadingViewHolder>{

    inner class LoadingViewHolder(binding: HomeFragmentComicRvBodyNewBinding) : BaseGlideLoadingViewHolder<HomeFragmentComicRvBodyNewBinding>(binding) {
        var mPathWord: String = ""
    }

    // 父布局高度
    private var mParentHeight: Int? = null

    // 名称高度
    private var mNameHeight: Int? = null

    // 初始化卡片内部视图
    private fun LoadingViewHolder.initView(pathword: String, name: String, imageUrl: String, author: List<AuthorResult>, hot: Int, lastestChapter: String?) {
        mPathWord = pathword                                                                                             // 设置路径值 （用于后续请求）

        rvBinding.homeComicRvLoading.isVisible = true
        rvBinding.homeComicRvProgressText.isVisible = true
        rvBinding.homeComicRvProgressText.text = AppGlideProgressFactory.PERCENT_0
        mAppGlideProgressFactory?.doRemoveListener()?.doClean()
        mAppGlideProgressFactory = AppGlideProgressFactory.createGlideProgressListener(imageUrl) { _, _, percentage, _, _ ->
            rvBinding.homeComicRvProgressText.text = AppGlideProgressFactory.getProgressString(percentage)
        }

        // 加载封面
        Glide.with(itemView)
            .load(imageUrl)
            .addListener(mAppGlideProgressFactory?.getRequestListener())
            .transition(GenericTransitionOptions<Drawable>().transition { dataSource, _ ->
                if (dataSource == DataSource.REMOTE) {
                    rvBinding.homeComicRvLoading.isInvisible = true
                    rvBinding.homeComicRvProgressText.isInvisible = true
                    DrawableCrossFadeTransition(BASE_ANIM_200L.toInt(), true)
                } else {
                    rvBinding.homeComicRvLoading.isInvisible = true
                    rvBinding.homeComicRvProgressText.isInvisible = true
                    NoTransition()
                }
            })
            .into(rvBinding.homeComicRvImage)
        rvBinding.homeComicRvName.text = name                                                                // 漫画名
        rvBinding.homeComicRvHot.text = formatValue(hot)                                                  // 热度 ： 12.3456 W

        // 作者 ：Crow
        if (rvBinding.homeComicRvAuthor.isVisible) {
            rvBinding.homeComicRvAuthor.text = author.joinToString { it.name }
        } else {
            rvBinding.homeComicRvAuthor.text = null
        }

        // 最新章节
        if (lastestChapter == null) rvBinding.homeComicRvLastestChapter.isVisible = false
        else {
            rvBinding.homeComicRvLastestChapter.isVisible = true
            rvBinding.homeComicRvLastestChapter.text = lastestChapter
        }
    }

    override fun getItemCount(): Int = mData.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoadingViewHolder {
        return LoadingViewHolder(HomeFragmentComicRvBodyNewBinding.inflate(from(parent.context), parent, false)).also { vh ->

            val isTopic = mType == Type.TOPIC

            // 漫画卡片高度
            val layoutParams = vh.rvBinding.homeComicRvImage.layoutParams
            layoutParams.width = (if (!isTopic) getComicCardWidth() else getComicCardWidth() / 2 + getComicCardWidth()) - mSize10
            layoutParams.height = getComicCardHeight()

            vh.rvBinding.homeComicRvName.doOnLayout { view ->
                if (mNameHeight == null) mNameHeight = if (vh.rvBinding.homeComicRvName.lineCount == 1) view.measuredHeight * 2 else view.measuredHeight
                (vh.rvBinding.homeComicRvName.layoutParams as LinearLayoutCompat.LayoutParams).height = mNameHeight!!
            }

            // 点击 父布局卡片 以及漫画卡片 事件 回调给上级 HomeFragment --> ContainerFragment
            vh.rvBinding.root.doOnClickInterval {
                if (isTopic) { /* TOOD TOPIC*/  }
                else doOnTap(vh.mPathWord)
            }
            vh.rvBinding.homeBookCardView.doOnClickInterval {
                if (isTopic) { /* TOOD TOPIC*/  }
                else doOnTap(vh.mPathWord)
            }

            // Tooltips漫画名称设置
            ToolTipsView.showToolTipsByLongClick(vh.rvBinding.homeComicRvName)
        }
    }

    override fun onBindViewHolder(vh: LoadingViewHolder, pos: Int) {
        when (mType) {
            Type.REC -> {
                // 推荐 设置底部间距0
                (vh.rvBinding.root.layoutParams as GridLayoutManager.LayoutParams).bottomMargin = 0
                val comic = (mData as MutableList<RecComicsResult>)[pos].mComic
                vh.initView(comic.mPathWord, comic.mName, comic.mImageUrl, comic.mAuthorResult, comic.mPopular, null)
            }
            Type.HOT -> {
                val comic = (mData as MutableList<HotComic>)[pos].mComic
                vh.initView(comic.mPathWord, comic.mName, comic.mImageUrl, comic.mAuthorResult, comic.mPopular, comic.mLastChapterName)
            }
            Type.NEW -> {
                val comic = (mData as MutableList<NewComic>)[pos].mComic
                vh.initView(comic.mPathWord, comic.mName, comic.mImageUrl, comic.mAuthorResult, comic.mPopular, comic.mLastChapterName)
            }
            Type.FINISH -> {
                val comic = (mData as MutableList<FinishComic>)[pos]
                vh.initView(comic.mPathWord, comic.mName, comic.mImageUrl, comic.mAuthorResult, comic.mPopular, null)
            }
            Type.RANK -> {
                val comic = (mData as MutableList<RankComics>)[pos].mComic
                vh.initView(comic.mPathWord, comic.mName, comic.mImageUrl, comic.mAuthorResult, comic.mPopular, null)
            }
            Type.TOPIC -> {
                val comic = (mData as MutableList<Topices>)[pos]
                Glide.with(vh.itemView).load(comic.mImageUrl).into(vh.rvBinding.homeComicRvImage)
                vh.mPathWord = comic.mPathWord
                vh.rvBinding.apply {
                    homeComicRvName.maxLines = 4
                    homeComicRvName.text = comic.mTitle
                    homeComicRvAuthor.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                    homeComicRvAuthor.text = comic.mDatetimeCreated
                    homeComicRvHot.visibility = View.GONE
                }
            }
        }
    }

    override fun setColor(vh: LoadingViewHolder, color: Int) {
        vh.rvBinding.homeComicRvName.setTextColor(color)
        vh.rvBinding.homeComicRvHot.setTextColor(color)
        vh.rvBinding.homeComicRvAuthor.setTextColor(color)
        vh.rvBinding.homeComicRvLastestChapter.setTextColor(color)
    }

    suspend fun doNotify(datas: MutableList<T>, delay: Long) {
        val isCountSame = itemCount == datas.size
        if (isCountSame) mData = datas
        else if(itemCount != 0) {
            notifyItemRangeRemoved(0, itemCount)
            mData.clear()
        }
        datas.forEachIndexed { index, data ->
            if (!isCountSame) {
                mData.add(data)
                notifyItemInserted(index)
            } else notifyItemChanged(index)
            delay(delay)
        }
    }
}
