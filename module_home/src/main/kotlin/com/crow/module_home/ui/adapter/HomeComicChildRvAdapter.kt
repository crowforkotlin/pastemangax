@file:Suppress("UNCHECKED_CAST")

package com.crow.module_home.ui.adapter

import android.view.LayoutInflater.from
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.crow.base.current_project.*
import com.crow.base.current_project.entity.BookTapEntity
import com.crow.base.current_project.entity.BookType
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.ui.view.ToolTipsView
import com.crow.module_home.databinding.HomeFragmentComicRvBodyBinding
import com.crow.module_home.model.resp.homepage.*
import com.crow.module_home.model.resp.homepage.results.AuthorResult
import com.crow.module_home.model.resp.homepage.results.RecComicsResult
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
    private val mBookType: BookType,
    val doOnTap: (BookTapEntity) -> Unit
) : RecyclerView.Adapter<HomeComicChildRvAdapter<T>.ViewHolder>() {

    inner class ViewHolder(val rvBinding: HomeFragmentComicRvBodyBinding) : RecyclerView.ViewHolder(rvBinding.root) { var mPathWord: String = "" }

    // 父布局高度
    private var mParentHeight: Int? = null

    // 初始化卡片内部视图
    private fun ViewHolder.initView(pathword: String, name: String, imageUrl: String, author: List<AuthorResult>, hot: Int) {
        Glide.with(itemView)
            .load(imageUrl)
            .into(rvBinding.homeComicRvImage)   // 加载封面
        rvBinding.homeComicRvName.text = name                                  // 漫画名
        rvBinding.homeComicRvAuthor.text = author.joinToString { it.name }     // 作者 ：Crow
        rvBinding.homeComicRvHot.text = formatValue(hot)                       // 热度 ： 12.3456 W
        mPathWord = pathword                                                   // 设置路径值 （用于后续请求）
    }

    override fun getItemCount(): Int = mData.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(HomeFragmentComicRvBodyBinding.inflate(from(parent.context), parent, false)).also { vh ->

            // 推荐 设置底部间距0
            if(mBookType == BookType.Rec) (vh.rvBinding.homeComicRvHot.layoutParams as ConstraintLayout.LayoutParams).bottomMargin = 0

            // 漫画卡片高度
            vh.rvBinding.homeComicRvImage.layoutParams.apply {
                width = (if (mBookType != BookType.Topic) getComicCardWidth() else getComicCardWidth() / 2 + getComicCardWidth()) - mSize10
                height = getComicCardHeight()
            }

            // 点击 父布局卡片 以及漫画卡片 事件 回调给上级 HomeFragment --> ContainerFragment
            vh.rvBinding.root.doOnClickInterval {
                if (mBookType == BookType.Topic) { }
                else doOnTap(BookTapEntity(BookType.Comic, vh.mPathWord))
            }
            vh.rvBinding.homeBookCard.doOnClickInterval {
                if (mBookType == BookType.Topic) { }
                else doOnTap(BookTapEntity(BookType.Comic, vh.mPathWord))
            }

            // Tooltips漫画名称设置
            ToolTipsView.showToolTipsByLongClick(vh.rvBinding.homeComicRvName)
        }
    }

    override fun onBindViewHolder(vh: ViewHolder, pos: Int) {
        when (mBookType) {
            BookType.Rec -> {
                val comic = (mData as MutableList<RecComicsResult>)[pos].mComic
                vh.initView(comic.mPathWord, comic.mName, comic.mImageUrl, comic.mAuthorResult, comic.mPopular)
            }
            BookType.Hot -> {
                val comic = (mData as MutableList<HotComic>)[pos].mComic
                vh.initView(comic.mPathWord, comic.mName, comic.mImageUrl, comic.mAuthorResult, comic.mPopular)
            }
            BookType.New -> {
                val comic = (mData as MutableList<NewComic>)[pos].mComic
                vh.initView(comic.mPathWord, comic.mName, comic.mImageUrl, comic.mAuthorResult, comic.mPopular)
            }
            BookType.Finish -> {
                val comic = (mData as MutableList<FinishComic>)[pos]
                vh.initView(comic.mPathWord, comic.mName, comic.mImageUrl, comic.mAuthorResult, comic.mPopular)
            }
            BookType.Rank -> {
                val comic = (mData as MutableList<RankComics>)[pos].mComic
                vh.initView(comic.mPathWord, comic.mName, comic.mImageUrl, comic.mAuthorResult, comic.mPopular)
            }
            BookType.Topic -> {
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
            else -> { }
        }
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
