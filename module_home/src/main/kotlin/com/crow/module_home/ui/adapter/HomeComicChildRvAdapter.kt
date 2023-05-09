@file:Suppress("UNCHECKED_CAST")

package com.crow.module_home.ui.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater.from
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.request.transition.DrawableCrossFadeTransition
import com.crow.base.copymanga.*
import com.crow.base.copymanga.entity.IBookAdapterColor
import com.crow.base.copymanga.glide.AppGlideProgressFactory
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.animateFadeOut
import com.crow.base.tools.extensions.doOnClickInterval

import com.crow.base.ui.adapter.BaseGlideViewHolder
import com.crow.base.ui.view.ToolTipsView
import com.crow.module_home.databinding.HomeFragmentComicRvBodyBinding
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
) : RecyclerView.Adapter<HomeComicChildRvAdapter<T>.ViewHolder>() , IBookAdapterColor<HomeComicChildRvAdapter<T>.ViewHolder>{

    inner class ViewHolder(binding: HomeFragmentComicRvBodyBinding) : BaseGlideViewHolder<HomeFragmentComicRvBodyBinding>(binding) {
        var mPathWord: String = ""
    }

    // 父布局高度
    private var mParentHeight: Int? = null

    override fun onViewRecycled(vh: ViewHolder) {
        super.onViewRecycled(vh)
        vh.mAppGlideProgressFactory?.doRemoveListener()?.doClean()
        vh.mAppGlideProgressFactory = null
    }


    // 初始化卡片内部视图
    private fun ViewHolder.initView(pathword: String, name: String, imageUrl: String, author: List<AuthorResult>, hot: Int, lastestChapter: String?) {
        mPathWord = pathword                                                                                             // 设置路径值 （用于后续请求）


        rvBinding.homeComicRvLoading.alpha = 1f
        rvBinding.homeComicRvProgressText.alpha = 1f
        mAppGlideProgressFactory?.doRemoveListener()?.doClean()
        mAppGlideProgressFactory = AppGlideProgressFactory.createGlideProgressListener(imageUrl) { _, _, percentage, _, _ ->
            rvBinding.homeComicRvProgressText.text = AppGlideProgressFactory.getProgressString(percentage)
        }

        // 加载封面
        Glide.with(itemView)
            .load(imageUrl)
            .addListener(mAppGlideProgressFactory?.getRequestListener())
            .transition(GenericTransitionOptions<Drawable>().transition { _, _ ->
                rvBinding.homeComicRvLoading.animateFadeOut()
                rvBinding.homeComicRvProgressText.animateFadeOut()
                DrawableCrossFadeTransition(BASE_ANIM_200L.toInt(), true)
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

        toSetColor(this, hot)
    }

    override fun getItemCount(): Int = mData.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(HomeFragmentComicRvBodyBinding.inflate(from(parent.context), parent, false)).also { vh ->

            // 推荐 设置底部间距0
            if(mType == Type.REC) (vh.rvBinding.homeComicRvHot.layoutParams as ConstraintLayout.LayoutParams).bottomMargin = 0

            val isTopic = mType == Type.TOPIC
            // 漫画卡片高度
            vh.rvBinding.homeComicRvImage.layoutParams.apply {
                width = (if (!isTopic) getComicCardWidth() else getComicCardWidth() / 2 + getComicCardWidth()) - mSize10
                height = getComicCardHeight()
            }

            // 点击 父布局卡片 以及漫画卡片 事件 回调给上级 HomeFragment --> ContainerFragment
            vh.rvBinding.root.doOnClickInterval {
                if (isTopic) { }
                else doOnTap(vh.mPathWord)
            }
            vh.rvBinding.homeBookCardView.doOnClickInterval {
                if (isTopic) { }
                else doOnTap(vh.mPathWord)
            }

            // Tooltips漫画名称设置
            ToolTipsView.showToolTipsByLongClick(vh.rvBinding.homeComicRvName)
        }
    }

    override fun onBindViewHolder(vh: ViewHolder, pos: Int) {
        when (mType) {
            Type.REC -> {
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
                    (homeComicRvAuthor.layoutParams as ConstraintLayout.LayoutParams).apply {
                        bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                    }
                    homeComicRvHot.visibility = View.GONE
                }
            }
        }
    }

    override fun setColor(vh: ViewHolder, color: Int) {
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
