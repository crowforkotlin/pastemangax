@file:Suppress("UNCHECKED_CAST")

package com.crow.module_home.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.app.appContext
import com.crow.base.extensions.logMsg
import com.crow.module_home.databinding.HomeRvBookParentBinding
import com.crow.module_home.model.ComicType
import com.crow.module_home.model.resp.homepage.*
import com.crow.module_home.model.resp.homepage.results.RecComicsResult
import com.crow.module_home.ui.fragment.HomeFragment
import com.orhanobut.logger.Logger
import java.util.*

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_home/src/main/kotlin/com/crow/module_home/ui/adapter
 * @Time: 2023/3/11 2:17
 * @Author: CrowForKotlin
 * @Description: HomeBookAdapter
 * @formatter:on
 **************************/
class HomeBookAdapter1<T>(
    private var mData: ArrayList<T>? = null,
    private val mTapComicListener: HomeFragment.TapComicListener
) : RecyclerView.Adapter<HomeBookAdapter1<T>.ViewHolder>() {

    private val mHomeRecAdapter: HomeBookAdapter2<ComicDatas<RecComicsResult>> by lazy { HomeBookAdapter2(null, ComicType.Rec, mTapComicListener) }
    private val mHomeHotAdapter: HomeBookAdapter2<List<HotComic>> by lazy { HomeBookAdapter2(null, ComicType.Hot, mTapComicListener) }
    private val mHomeNewAdapter: HomeBookAdapter2<List<NewComic>> by lazy { HomeBookAdapter2(null, ComicType.New, mTapComicListener) }
    private val mHomeCommitAdapter: HomeBookAdapter2<FinishComicDatas> by lazy { HomeBookAdapter2(null, ComicType.Commit, mTapComicListener) }
    private val mHomeTopicAapter: HomeBookAdapter2<ComicDatas<Topices>> by lazy { HomeBookAdapter2(null, ComicType.Topic, mTapComicListener) }
    private val mHomeRankAapter: HomeBookAdapter2<ComicDatas<RankComics>> by lazy { HomeBookAdapter2(null, ComicType.Rank, mTapComicListener) }

    inner class ViewHolder(val rvBinding: HomeRvBookParentBinding) : RecyclerView.ViewHolder(rvBinding.root) { var mPathWord: String = "" }

    // 漫画卡片高度
    private val mChildCardHeight: Int = run {
        val width = appContext.resources.displayMetrics.widthPixels
        val height = appContext.resources.displayMetrics.heightPixels
        (width.toFloat() / (3 - width.toFloat() / height.toFloat())).toInt()
    }


    // 父布局高度
    private var mParentHeight: Int? = null

    override fun getItemCount(): Int = if (mData == null) 0 else mData!!.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(HomeRvBookParentBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }
    override fun onBindViewHolder(vh: ViewHolder, pos: Int) {
        mHomeRecAdapter.getDataSize().logMsg(level = Logger.ERROR)
        mHomeHotAdapter.getDataSize().logMsg(level = Logger.ERROR)
        mHomeNewAdapter.getDataSize().logMsg(level = Logger.ERROR)
        mHomeCommitAdapter.getDataSize().logMsg(level = Logger.ERROR)
        mHomeTopicAapter.getDataSize().logMsg(level = Logger.ERROR)
        mHomeRankAapter.getDataSize().logMsg(level = Logger.ERROR)
        when (pos) {
            0 -> {
                val comic = (mData!![pos] as ComicDatas<RecComicsResult>)
                vh.rvBinding.homeItemBookRv.adapter = mHomeRecAdapter
                if (mHomeRecAdapter.getDataSize() == 0) {
                    mHomeRecAdapter.setData(comic, 3)
                    mHomeRecAdapter.notifyDataSetChanged()
                }
            }
            1 -> {
                val comic = (mData!![pos] as List<HotComic>)
                vh.rvBinding.homeItemBookRv.adapter = mHomeHotAdapter
                if (mHomeHotAdapter.getDataSize() == 0) {
                    mHomeHotAdapter.setData(comic, 3)
                    mHomeHotAdapter.notifyDataSetChanged()
                }
            }
            2 -> {
                val comic = (mData!![pos] as List<NewComic>)
                vh.rvBinding.homeItemBookRv.adapter = mHomeNewAdapter
                if (mHomeNewAdapter.getDataSize() == 0) {
                    mHomeNewAdapter.setData(comic, 3)
                    mHomeNewAdapter.notifyDataSetChanged()
                }
            }
            3 -> {
                val comic = (mData!![pos] as FinishComicDatas)
                vh.rvBinding.homeItemBookRv.adapter = mHomeCommitAdapter
                if (mHomeCommitAdapter.getDataSize() == 0) {
                    mHomeCommitAdapter.setData(comic, 3)
                    mHomeCommitAdapter.notifyDataSetChanged()
                }
            }
            4 -> {
                val comic = (mData!![pos] as ComicDatas<Topices>)
                vh.rvBinding.homeItemBookRv.adapter = mHomeTopicAapter
                if (mHomeTopicAapter.getDataSize() == 0) {
                    mHomeTopicAapter.notifyDataSetChanged()
                    mHomeTopicAapter.setData(comic, 3)
                }

            }
            5 -> {
                val comic = (mData!![pos] as ComicDatas<RankComics>)
                vh.rvBinding.homeItemBookRv.adapter = mHomeRankAapter
                if (mHomeRankAapter.getDataSize() == 0) {
                    mHomeRankAapter.setData(comic, 3)
                    mHomeRankAapter.notifyDataSetChanged()
                }
            }
            else -> { }
        }
    }
    private fun <T> HomeRvBookParentBinding.initHomeItem(@DrawableRes iconRes: Int, @StringRes iconText: Int, adapter: HomeBookAdapter2<T>): HomeRvBookParentBinding {
        homeItemBt.setIconResource(iconRes)
        homeItemBt.text = appContext.getString(iconText)
        homeItemBookRv.adapter = adapter
        return this
    }
    // 对外暴露设置数据
    fun setData(value: ArrayList<T>) { mData = value }
}