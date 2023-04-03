@file:Suppress("UNCHECKED_CAST")

package com.crow.module_home.ui.adapter

import android.view.LayoutInflater.from
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.crow.base.R
import com.crow.base.app.appContext
import com.crow.base.current_project.entity.BookType
import com.crow.base.current_project.formatValue
import com.crow.base.current_project.getComicCardHeight
import com.crow.base.current_project.getComicCardWidth
import com.crow.base.tools.extensions.clickGap
import com.crow.base.ui.view.ToolTipsView
import com.crow.module_home.databinding.HomeFragmentComicRvBinding
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
class HomeComicRvAdapter<T>(
    private var mData: MutableList<T> = mutableListOf(),
    private val mType: BookType,
    inline val doOnTap: (BookType, String) -> Unit
) : RecyclerView.Adapter<HomeComicRvAdapter<T>.ViewHolder>() {

    inner class ViewHolder(val rvBinding: HomeFragmentComicRvBinding) : RecyclerView.ViewHolder(rvBinding.root) { var mPathWord: String = "" }

    // 父布局高度
    private var mParentHeight: Int? = null

    override fun getItemCount(): Int = mData.size

    private val mSize10 = appContext.resources.getDimension(R.dimen.base_dp10).toInt()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(HomeFragmentComicRvBinding.inflate(from(parent.context), parent, false)).also { vh ->

            // 推荐 设置底部间距0
            if(mType == BookType.Rec) (vh.rvBinding.homeComicRvHot.layoutParams as ConstraintLayout.LayoutParams).bottomMargin = 0

            // 漫画卡片高度
            vh.rvBinding.homeComicRvImage.layoutParams.apply {
                width = if (mType != BookType.Topic) getComicCardWidth() else getComicCardWidth() / 2 + getComicCardWidth()
                height = getComicCardHeight()
            }

            // 点击 父布局卡片 以及漫画卡片 事件 回调给上级 HomeFragment --> ContainerFragment
            vh.rvBinding.root.clickGap { _, _ -> doOnTap(mType, vh.mPathWord) }
            vh.rvBinding.homeBookCard.clickGap { _, _ -> doOnTap(mType, vh.mPathWord) }

            // Tooltips漫画名称设置
            ToolTipsView.showToolTipsByLongClick(vh.rvBinding.homeComicRvName)
        }
    }

    override fun onBindViewHolder(vh: ViewHolder, pos: Int) {
        when (mType) {
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

    // 初始化卡片内部视图
    private fun ViewHolder.initView(pathword: String, name: String, imageUrl: String, author: List<AuthorResult>, hot: Int) {
        Glide.with(itemView).load(imageUrl).into(rvBinding.homeComicRvImage)   // 加载封面
        rvBinding.homeComicRvName.text = name                                  // 漫画名
        rvBinding.homeComicRvAuthor.text = author.joinToString { it.name }     // 作者 ：Crow
        rvBinding.homeComicRvHot.text = formatValue(hot)                       // 热度 ： 12.3456 W
        mPathWord = pathword                                                   // 设置路径值 （用于后续请求）
    }


    /*
    * 下面的代码逻辑全都一致，整合到一个Rv文件中不需要分开，看着有点屎山可能 有改进可在联系我提交 有考虑用ListAdapter，但还是为了整合到第一个文件 默认就改之前写好的这个了
    * 1：首先判断 Rv旧数据源 和 新 数据源 的大小是否相等
    * 2：相同则 覆盖旧的值 否则在判断旧数据值是否不为0（这里为什么加这个判断 以防万一Rv数据加载失败 新旧数据大小不一致就可以通知移除所有数据）
    * 3：开始循环 -> 数据大小不一致（代表需要新增数据） 否则 通知数据做出改变即可
    * 4：延时：第一次进入界面延时（20L-视觉上直接刷新完）后面刷新用50L（动态的有一个反馈效果），20L不适合手动刷新，会有错位Rv的视觉效果bug
    * 5：开启协程避免阻塞UI线程
    * @Time: 2023.3.22
    * @Author: CrowForKotlin
    * */

    suspend fun doRecNotify(adapter: HomeComicRvAdapter<RecComicsResult>, newDataResult: MutableList<RecComicsResult>, delay: Long) {
        val isCountSame = itemCount == newDataResult.size
        if (isCountSame) adapter.mData = newDataResult
        else if(itemCount != 0) {
            adapter.notifyItemRangeRemoved(0, itemCount)
            adapter.mData.clear()
        }
        newDataResult.forEachIndexed { index, data ->
            if (!isCountSame) {
                adapter.mData.add(data)
                adapter.notifyItemInserted(index)
            } else adapter.notifyItemChanged(index)
            delay(delay)
        }
    }

    suspend fun doHotNotify(adapter: HomeComicRvAdapter<HotComic>, datas: MutableList<HotComic>, delay: Long) {
        val isSizeSame = itemCount == datas.size
        if (isSizeSame) adapter.mData = datas
        else if(itemCount != 0) {
            adapter.notifyItemRangeRemoved(0, itemCount)
            adapter.mData.clear()
        }
        datas.forEachIndexed { index, data ->
            if (!isSizeSame) {
                adapter.mData.add(data)
                adapter.notifyItemInserted(index)
            } else adapter.notifyItemChanged(index)
            delay(delay)
        }
    }

    suspend fun doNewNotify(adapter: HomeComicRvAdapter<NewComic>, datas: MutableList<NewComic>, delay: Long) {
        val isSizeSame = itemCount == datas.size
        if (isSizeSame) adapter.mData = datas
        else if(itemCount != 0) {
            adapter.notifyItemRangeRemoved(0, itemCount)
            adapter.mData.clear()
        }
        datas.forEachIndexed { index, data ->
            if (!isSizeSame) {
                adapter.mData.add(data)
                adapter.notifyItemInserted(index)
            } else adapter.notifyItemChanged(index)
            delay(delay)
        }
    }

    suspend fun doFinishNotify(adapter: HomeComicRvAdapter<FinishComic>, datas: MutableList<FinishComic>, delay: Long) {
        val isSizeSame = itemCount == datas.size
        if (isSizeSame) adapter.mData = datas
        else if(itemCount != 0) {
            adapter.notifyItemRangeRemoved(0, itemCount)
            adapter.mData.clear()
        }
        datas.forEachIndexed { index, data ->
            if (!isSizeSame) {
                adapter.mData.add(data)
                adapter.notifyItemInserted(index)
            } else adapter.notifyItemChanged(index)
            delay(delay)
        }
    }

    suspend fun doRankNotify(adapter: HomeComicRvAdapter<RankComics>, datas: MutableList<RankComics>, delay: Long) {
        val isSizeSame = itemCount == datas.size
        if (isSizeSame) adapter.mData = datas
        else if(itemCount != 0) {
            adapter.notifyItemRangeRemoved(0, itemCount)
            adapter.mData.clear()
        }
        datas.forEachIndexed { index, data ->
            if (!isSizeSame) {
                adapter.mData.add(data)
                adapter.notifyItemInserted(index)
            } else adapter.notifyItemChanged(index)
            delay(delay)
        }
    }

    suspend fun doTopicNotify(adapter: HomeComicRvAdapter<Topices>, datas: MutableList<Topices>, delay: Long) {
        val isSizeSame = itemCount == datas.size
        if (isSizeSame) adapter.mData = datas
        else if(itemCount != 0) {
            adapter.notifyItemRangeRemoved(0, itemCount)
            adapter.mData.clear()
        }
        datas.forEachIndexed { index, data ->
            if (!isSizeSame) {
                adapter.mData.add(data)
                adapter.notifyItemInserted(index)
            } else adapter.notifyItemChanged(index)
            delay(delay)
        }
    }
}
