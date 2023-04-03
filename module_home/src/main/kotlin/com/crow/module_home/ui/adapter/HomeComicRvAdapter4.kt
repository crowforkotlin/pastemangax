@file:Suppress("UNCHECKED_CAST")

package com.crow.module_home.ui.adapter

import android.view.LayoutInflater.from
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.crow.base.current_project.entity.BookType
import com.crow.base.current_project.formatValue
import com.crow.base.current_project.getComicCardHeight
import com.crow.base.current_project.getComicCardWidth
import com.crow.base.tools.extensions.clickGap
import com.crow.base.ui.view.ToolTipsView
import com.crow.module_home.databinding.HomeFragmentBanner2Binding
import com.crow.module_home.databinding.HomeFragmentComicRvBinding
import com.crow.module_home.model.resp.homepage.*
import com.crow.module_home.model.resp.homepage.results.AuthorResult
import com.crow.module_home.model.resp.homepage.results.Results
import java.util.*

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_home/src/main/kotlin/com/crow/module_home/ui/adapter
 * @Time: 2023/3/11 2:17
 * @Author: CrowForKotlin
 * @Description: HomeBookAdapter
 * @formatter:on
 **************************/
class HomeComicRvAdapter4(
    private var mResults: Results,
    private val mBannerSize: Int,
    private val mRecSize: Int,
    private val mHotSize: Int,
    private val mNewSize: Int,
    private val mFinishSize: Int,
    private val mRankSize: Int,
    private val mTopicSize: Int,
    inline val doOnTap: (BookType, String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class ComicViewHolder(val rvBinding: HomeFragmentComicRvBinding) : RecyclerView.ViewHolder(rvBinding.root) { var mPathWord: String = "" }
    inner class BannerViewHolder(val rvBinding: HomeFragmentBanner2Binding) : RecyclerView.ViewHolder(rvBinding.root) { var mPathWord: String = "" }

    // 父布局高度
    private var mParentHeight: Int? = null

    private val mTotalSize = mBannerSize + mRecSize + mHotSize + mNewSize + mFinishSize + mRankSize + mTopicSize + 6

    // 初始化卡片内部视图
    private fun ComicViewHolder.initView(pathword: String, name: String, imageUrl: String, author: List<AuthorResult>, hot: Int) {
        Glide.with(itemView).load(imageUrl).into(rvBinding.homeComicRvImage)   // 加载封面
        rvBinding.homeComicRvName.text = name                                  // 漫画名
        rvBinding.homeComicRvAuthor.text = author.joinToString { it.name }     // 作者 ：Crow
        rvBinding.homeComicRvHot.text = formatValue(hot)                       // 热度 ： 12.3456 W
        mPathWord = pathword                                                   // 设置路径值 （用于后续请求）
    }

    override fun getItemCount(): Int = mTotalSize

    override fun getItemViewType(position: Int) = position

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ComicViewHolder(HomeFragmentComicRvBinding.inflate(from(parent.context), parent, false)).also { vh ->

            // 漫画卡片高度
            vh.rvBinding.homeComicRvImage.layoutParams.apply {
                width = getComicCardWidth() / 2 + getComicCardWidth()
                height = getComicCardHeight()
            }

            // 点击 父布局卡片 以及漫画卡片 事件 回调给上级 HomeFragment --> ContainerFragment
            vh.rvBinding.root.clickGap { _, _ ->  }
            vh.rvBinding.homeBookCard.clickGap { _, _ ->  }

            // Tooltips漫画名称设置
            ToolTipsView.showToolTipsByLongClick(vh.rvBinding.homeComicRvName)
        }
    }

    override fun onBindViewHolder(vh: RecyclerView.ViewHolder, pos: Int) {

        if (vh is ComicViewHolder) {


            return
        }
        if (vh is BannerViewHolder) {

        }
    }

}
