@file:Suppress("UNCHECKED_CAST", "FunctionName", "NonAsciiCharacters", "CAST_NEVER_SUCCEEDS")

package com.crow.module_home.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater.from
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.app.appContext
import com.crow.base.current_project.entity.BookType
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.clickGap
import com.crow.base.tools.extensions.logMsg
import com.crow.module_home.R
import com.crow.module_home.databinding.HomeFragmentBanner2Binding
import com.crow.module_home.databinding.HomeFragmentComicRv2Binding
import com.crow.module_home.databinding.HomeFragmentComicRvHeaderBinding
import com.crow.module_home.databinding.HomeFragmentComicRvRecRefreshBinding
import com.crow.module_home.model.resp.homepage.*
import com.crow.module_home.model.resp.homepage.results.RecComicsResult
import com.google.android.material.button.MaterialButton
import com.to.aboomy.pager2banner.IndicatorView
import com.to.aboomy.pager2banner.ScaleInTransformer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.crow.base.R as baseR

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_home/src/main/kotlin/com/crow/module_home/ui/adapter
 * @Time: 2023/4/3 2:50
 * @Author: CrowForKotlin
 * @Description: HomeBookAdapter
 * @formatter:on
 **************************/

class HomeComicRvAdapter2(
    private var mData: MutableList<Any?>? = null,
    private val viewLifecycleOwner: LifecycleOwner,
    inline val doOnRecRefresh: (MaterialButton) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class ComicHeaderViewHolder(val rvBinding: HomeFragmentComicRvHeaderBinding) : RecyclerView.ViewHolder(rvBinding.root) { var mPathWord: String = "" }

    inner class ComicBodyViewHolder(val rvBinding: HomeFragmentComicRv2Binding) : RecyclerView.ViewHolder(rvBinding.root) { var mPathWord: String = "" }

    inner class BannerViewHolder(val rvBinding: HomeFragmentBanner2Binding) : RecyclerView.ViewHolder(rvBinding.root) { var mPathWord: String = "" }

    inner class ComicRecRefreshViewHolder(val rvBinding: HomeFragmentComicRvRecRefreshBinding) : RecyclerView.ViewHolder(rvBinding.root)



    private var mHomeRecComicRvAdapter: HomeComicRvAdapter3<RecComicsResult>? = null
    private var mIsRefresh = false
    private lateinit var mHomeBannerRvAdapter: HomeBannerRvAdapter
    private var mRvDelayMs: Long = 50L

    override fun getItemCount(): Int = mData?.size ?: 0

    override fun getItemViewType(position: Int) = position

    override fun onBindViewHolder(vh: RecyclerView.ViewHolder, pos: Int) {
        if (mIsRefresh) {
            pos.logMsg()
            when(pos) {
                0 -> (vh as BannerViewHolder).rvBinding.homeFragmentBanner2.adapter = HomeBannerRvAdapter((mData!![pos] as MutableList<Banner>)) { _, _ ->}
                2 -> (vh as ComicBodyViewHolder).rvBinding.homeFragmentComicRv2.adapter = HomeComicRvAdapter3((mData!![pos] as MutableList<RecComicsResult>), viewLifecycleOwner, BookType.Rec).also { mHomeRecComicRvAdapter = it }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> 创建轮播图持有者(parent)
            1 -> 创建漫画头部持有者(parent, R.drawable.home_ic_recommed_24dp, R.string.home_recommend_comic)
            2 -> 创建漫画内容持有者<RecComicsResult>(parent, BookType.Rec, viewType)
            3 -> 创建推荐换一批按钮(parent).also { it.rvBinding.homeComicRvRecRefresh.clickGap { _, _ -> doOnRecRefresh(it.rvBinding.homeComicRvRecRefresh) } }
            4 -> 创建漫画头部持有者(parent, R.drawable.home_ic_hot_24dp, R.string.home_hot_comic)
            5 -> 创建漫画内容持有者<HotComic>(parent, BookType.Hot, viewType)
            6 -> 创建漫画头部持有者(parent, R.drawable.home_ic_new_24dp, R.string.home_new_comic)
            7 -> 创建漫画内容持有者<NewComic>(parent, BookType.New, viewType)
            8 -> 创建漫画头部持有者(parent, R.drawable.home_ic_finish_24dp, R.string.home_commit_finish)
            9 -> 创建漫画内容持有者<FinishComic>(parent, BookType.Finish, viewType)
            10 -> 创建漫画头部持有者(parent, R.drawable.home_ic_rank_24dp, R.string.home_rank_comic)
            11 -> 创建漫画内容持有者<RankComics>(parent, BookType.Rank, viewType)
            12 -> 创建漫画头部持有者(parent, R.drawable.home_ic_topic_24dp, R.string.home_topic_comic)
            else -> 创建漫画内容持有者<Topices>(parent, BookType.Topic, viewType).also {
                it.rvBinding.homeFragmentComicRv2.layoutManager = GridLayoutManager(parent.context, 2)
            }
        }
    }

    private fun 创建轮播图持有者(parent: ViewGroup): RecyclerView.ViewHolder {
        val base20 = appContext.resources.getDimensionPixelSize(baseR.dimen.base_dp20)
        val vh = BannerViewHolder(HomeFragmentBanner2Binding.inflate(from(parent.context), parent, false))
        vh.doBannerNotify(0)
        vh.rvBinding.homeFragmentBanner2.doOnLayout { it.layoutParams.height = (it.width / 1.875 + 0.5).toInt() }
        vh.rvBinding.homeFragmentBanner2.addPageTransformer(ScaleInTransformer())
            .setPageMargin(base20, appContext.resources.getDimensionPixelSize(baseR.dimen.base_dp10))
            .setIndicator(
                IndicatorView(vh.itemView.context)
                    .setIndicatorColor(Color.DKGRAY)
                    .setIndicatorSelectorColor(Color.WHITE)
                    .setIndicatorStyle(IndicatorView.IndicatorStyle.INDICATOR_BEZIER)
                    .also { it.setPadding(0, 0, 0, base20) })
        return vh
    }

    private fun 创建漫画头部持有者(parent: ViewGroup, @DrawableRes icon: Int, @StringRes title: Int): RecyclerView.ViewHolder {
        val vh = ComicHeaderViewHolder(HomeFragmentComicRvHeaderBinding.inflate(from(parent.context), parent, false))
        vh.doInitComicHeader(icon, title)
        return vh
    }

    private fun<T> 创建漫画内容持有者(parent: ViewGroup, bookType: BookType, viewType: Int): ComicBodyViewHolder {
        val vh = ComicBodyViewHolder(HomeFragmentComicRv2Binding.inflate(from(parent.context), parent, false))
        vh.doComicNotify<T>(bookType, viewType)
        return vh
    }

    private fun 创建推荐换一批按钮(parent: ViewGroup) : ComicRecRefreshViewHolder {
        return ComicRecRefreshViewHolder(HomeFragmentComicRvRecRefreshBinding.inflate(from(parent.context), parent, false)).also { vh ->
            vh.rvBinding.root.animateFadeIn()
        }
    }


    private fun<T> HomeComicRvAdapter2.ComicBodyViewHolder.doComicNotify(bookType: BookType, pos: Int, delay: Long = mRvDelayMs) {
        val adapter = HomeComicRvAdapter3<T>(viewLifecycleOwner = viewLifecycleOwner, mBookType = bookType)
        if (bookType == BookType.Rec) mHomeRecComicRvAdapter = adapter as HomeComicRvAdapter3<RecComicsResult>
        viewLifecycleOwner.lifecycleScope.launch {
            rvBinding.homeFragmentComicRv2.adapter = adapter
            adapter.doNotify((mData!![pos] as MutableList<T>), delay)
            rvBinding.root.animateFadeIn()
        }
    }

    private fun HomeComicRvAdapter2.BannerViewHolder.doBannerNotify(pos: Int, delay: Long = mRvDelayMs) {
        val adapter = HomeBannerRvAdapter { _, _ -> }
        viewLifecycleOwner.lifecycleScope.launch {
            rvBinding.homeFragmentBanner2.adapter = adapter
            adapter.doBannerNotify((mData!![pos] as MutableList<Banner>), 0L)
            rvBinding.root.animateFadeIn()
        }
    }

    private fun HomeComicRvAdapter2.ComicHeaderViewHolder.doInitComicHeader(@DrawableRes iconRes: Int, @StringRes text: Int) {
        rvBinding.homeComicButtonTitle.icon = ContextCompat.getDrawable(appContext, iconRes)
        rvBinding.homeComicButtonTitle.text = appContext.getString(text)
        rvBinding.root.animateFadeIn()
    }

    suspend fun doNotify(newDataResult: MutableList<Any?>, isRefresh: Boolean = false, delayMs: Long = 100L, rvDelayMs: Long = 50L) {
        this.mIsRefresh = isRefresh
        this.mRvDelayMs = rvDelayMs
        val isCountSame = itemCount == newDataResult.size
        if (isCountSame) mData = newDataResult
        else {
            notifyItemRangeRemoved(0, itemCount)
            mData?.clear()
            delay(BASE_ANIM_200L)
        }
        newDataResult.forEachIndexed { index, data ->
            if (!isCountSame) {
                mData?.add(data)
                notifyItemInserted(index)
            } else notifyItemChanged(index)
            delay(delayMs)
        }
        this.mIsRefresh = false
    }

    suspend fun doRecNotify(datas: MutableList<RecComicsResult>, notifyMs: Long = 25L) {
        "notify $mHomeRecComicRvAdapter".logMsg()
        mHomeRecComicRvAdapter?.doNotify(datas, notifyMs)
    }
}
