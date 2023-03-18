@file:Suppress("UNCHECKED_CAST")

package com.crow.module_home.ui.adapter

import android.view.LayoutInflater.from
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.crow.base.extensions.ComicCardHeight
import com.crow.base.extensions.clickGap
import com.crow.base.extensions.formatValue
import com.crow.base.view.ToolTipsView
import com.crow.module_home.databinding.HomeComicRvBinding
import com.crow.module_home.databinding.HomeComicRvBinding.inflate
import com.crow.module_home.model.ComicType
import com.crow.module_home.model.resp.homepage.*
import com.crow.module_home.model.resp.homepage.results.AuthorResult
import com.crow.module_home.model.resp.homepage.results.RecComicsResult
import com.crow.module_home.ui.fragment.HomeFragment
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
class HomeComicAdapter<T>(
    private var mData: T? = null,
    private val mType: ComicType,
    private val mTapComicListener: HomeFragment.TapComicListener,
) : RecyclerView.Adapter<HomeComicAdapter<T>.ViewHolder>() {

    inner class ViewHolder(val rvBinding: HomeComicRvBinding) : RecyclerView.ViewHolder(rvBinding.root) { var mPathWord: String = "" }

    // 适配器数据量
    private var mSize: Int = 0

    // 父布局高度
    private var mParentHeight: Int? = null

    override fun getItemCount(): Int = if (mData == null) 0 else mSize
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(inflate(from(parent.context), parent, false)).also { vh ->

            // 漫画卡片高度
            vh.rvBinding.homeComicRvImage.doOnLayout { vh.rvBinding.homeComicRvImage.layoutParams.height = ComicCardHeight }

            // 设置父布局 固定高度 （因为最外层还有一个父布局卡片布局设置的时WRAP_CONTENT 根据 子控件决定高度的）
            vh.rvBinding.root.doOnLayout { rooView ->
                mParentHeight = mParentHeight ?: rooView.height
                rooView.layoutParams.height = mParentHeight!!
            }

            // 点击 父布局卡片 以及漫画卡片 事件 回调给上级 HomeFragment --> ContainerFragment
            vh.rvBinding.root.clickGap { _, _ -> mTapComicListener.onTap(mType, vh.mPathWord) }
            vh.rvBinding.homeBookCard.clickGap { _, _ -> mTapComicListener.onTap(mType, vh.mPathWord) }

            ToolTipsView.showToolTipsByLongClick(vh.rvBinding.homeComicRvName)
        }
    }
    override fun onBindViewHolder(vh: ViewHolder, pos: Int) {
        when (mType) {
            ComicType.Rec -> {
                val comic = (mData as ComicDatas<RecComicsResult>).mResult[pos].mComic
                vh.initView(comic.mPathWord, comic.mName, comic.mImageUrl, comic.mAuthorResult, comic.mPopular)
            }
            ComicType.Hot -> {
                val comic = (mData as List<HotComic>)[pos].mComic
                vh.initView(comic.mPathWord, comic.mName, comic.mImageUrl, comic.mAuthorResult, comic.mPopular)
            }
            ComicType.New -> {
                val comic = (mData as List<NewComic>)[pos].mComic
                vh.initView(comic.mPathWord, comic.mName, comic.mImageUrl, comic.mAuthorResult, comic.mPopular)
            }
            ComicType.Commit -> {
                val comic = (mData as FinishComicDatas).mResult[pos]
                vh.initView(comic.mPathWord, comic.mName, comic.mImageUrl, comic.mAuthorResult, comic.mPopular)
            }
            ComicType.Topic -> {
                val comic = (mData as ComicDatas<Topices>).mResult[pos]
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
            ComicType.Rank -> {
                val comic = (mData as ComicDatas<RankComics>).mResult[pos].mComic
                vh.initView(comic.mPathWord, comic.mName, comic.mImageUrl, comic.mAuthorResult, comic.mPopular)
            }
            else -> { }
        }
    }

    // 初始化卡片内部视图
    private fun ViewHolder.initView(pathword: String, name: String, imageUrl: String, author: List<AuthorResult>, hot: Int) {
        Glide.with(itemView).load(imageUrl).into(rvBinding.homeComicRvImage)   // 加载封面
        rvBinding.homeComicRvName.text = name                                  // 漫画名
        rvBinding.homeComicRvAuthor.text = author.joinToString { it.name }     // 作者 ：Crow
        rvBinding.homeComicRvHot.text = formatValue(hot)                       // 热度 ： 12.3456 W
        mPathWord = pathword                                                   // 设置路径值 （用于后续请求）
    }

    // 对外暴露设置数据
    fun setData(value: T, size: Int? = null) {
        mData = value
        if (size != null) this.mSize = size
    }

    // 对外暴露数据大小
    fun getDataSize() = mSize

    suspend fun doOnNotify(delay: Long = 50L, waitTime: Long = 100L) {
        repeat(mSize) {
            notifyItemChanged(it)
            delay(delay)
        }
        delay(waitTime)
    }
}