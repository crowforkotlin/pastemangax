@file:Suppress("UNCHECKED_CAST", "FunctionName", "NonAsciiCharacters", "CAST_NEVER_SUCCEEDS")

package com.crow.module_home.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater.from
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.app.appContext
import com.crow.base.copymanga.entity.BookTapEntity
import com.crow.base.copymanga.entity.BookType
import com.crow.base.copymanga.entity.BookType.Comic
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.module_home.R
import com.crow.module_home.databinding.HomeFragmentBannerRvBinding
import com.crow.module_home.databinding.HomeFragmentComicRvBinding
import com.crow.module_home.databinding.HomeFragmentComicRvHeaderBinding
import com.crow.module_home.databinding.HomeFragmentComicRvRecRefreshBinding
import com.crow.module_home.model.resp.homepage.Banner
import com.crow.module_home.model.resp.homepage.FinishComic
import com.crow.module_home.model.resp.homepage.HotComic
import com.crow.module_home.model.resp.homepage.NewComic
import com.crow.module_home.model.resp.homepage.RankComics
import com.crow.module_home.model.resp.homepage.Topices
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

class HomeComicParentRvAdapter(
    private var mData: MutableList<Any?>? = null,
    private val viewLifecycleOwner: LifecycleOwner,
    private val doOnRecRefresh: (MaterialButton) -> Unit,
    val doOnTap: (BookTapEntity) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class ComicHeaderViewHolder(val rvBinding: HomeFragmentComicRvHeaderBinding) : RecyclerView.ViewHolder(rvBinding.root) { var mPathWord: String = "" }

    inner class ComicBodyViewHolder(val rvBinding: HomeFragmentComicRvBinding) : RecyclerView.ViewHolder(rvBinding.root) { var mPathWord: String = "" }

    inner class BannerViewHolder(val rvBinding: HomeFragmentBannerRvBinding) : RecyclerView.ViewHolder(rvBinding.root) { var mPathWord: String = "" }

    inner class ComicRecRefreshViewHolder(val rvBinding: HomeFragmentComicRvRecRefreshBinding) : RecyclerView.ViewHolder(rvBinding.root)

    private var mHomeRecComicRvAdapter: HomeComicChildRvAdapter<RecComicsResult>? = null
    private var mIsRefresh = false
    private var mRvDelayMs: Long = 50L

    override fun getItemCount(): Int = mData?.size ?: 0

    override fun getItemViewType(position: Int) = position

    override fun onBindViewHolder(vh: RecyclerView.ViewHolder, pos: Int) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> 创建轮播图持有者(parent)
            1 -> 创建漫画头部持有者(parent, R.drawable.home_ic_recommed_24dp, R.string.home_recommend_comic)
            2 -> 创建漫画内容持有者<RecComicsResult>(parent, BookType.Rec, viewType)
            3 -> 创建推荐换一批按钮(parent).also { it.rvBinding.homeComicRvRecRefresh.doOnClickInterval { _ -> doOnRecRefresh(it.rvBinding.homeComicRvRecRefresh) } }
            4 -> 创建漫画头部持有者(parent, R.drawable.home_ic_hot_24dp, R.string.home_hot_comic)
            5 -> 创建漫画内容持有者<HotComic>(parent, BookType.Hot, viewType)
            6 -> 创建漫画头部持有者(parent, R.drawable.home_ic_new_24dp, R.string.home_new_comic)
            7 -> 创建漫画内容持有者<NewComic>(parent, BookType.New, viewType)
            8 -> 创建漫画头部持有者(parent, R.drawable.home_ic_finish_24dp, R.string.home_commit_finish)
            9 -> 创建漫画内容持有者<FinishComic>(parent, BookType.Finish, viewType)
            10 -> 创建漫画头部持有者(parent, R.drawable.home_ic_rank_24dp, R.string.home_rank_comic)
            11 -> 创建漫画内容持有者<RankComics>(parent, BookType.Rank, viewType)
            12 -> 创建漫画头部持有者(parent, R.drawable.home_ic_topic_24dp, R.string.home_topic_comic)
            else -> 创建漫画内容持有者<Topices>(parent, BookType.Topic, viewType).also { it.rvBinding.homeComicRv.layoutManager = GridLayoutManager(parent.context, 2) }
        }
    }

    private fun 创建轮播图持有者(parent: ViewGroup): RecyclerView.ViewHolder {
        val base20 = appContext.resources.getDimensionPixelSize(baseR.dimen.base_dp20)
        val vh = BannerViewHolder(HomeFragmentBannerRvBinding.inflate(from(parent.context), parent, false))
        vh.rvBinding.homeBannerRv.doOnLayout { it.layoutParams.height = (it.width / 1.875 + 0.5).toInt() }
        vh.rvBinding.homeBannerRv.addPageTransformer(ScaleInTransformer())
            .setPageMargin(base20, appContext.resources.getDimensionPixelSize(baseR.dimen.base_dp10))
            .setIndicator(
                IndicatorView(vh.itemView.context)
                    .setIndicatorColor(Color.DKGRAY)
                    .setIndicatorSelectorColor(Color.WHITE)
                    .setIndicatorStyle(IndicatorView.IndicatorStyle.INDICATOR_BEZIER)
                    .also { it.setPadding(0, 0, 0, base20) })
        vh.doBannerNotify(0)
        return vh
    }

    private fun 创建漫画头部持有者(parent: ViewGroup, @DrawableRes icon: Int, @StringRes title: Int): RecyclerView.ViewHolder {
        val vh = ComicHeaderViewHolder(HomeFragmentComicRvHeaderBinding.inflate(from(parent.context), parent, false))
        vh.doInitComicHeader(icon, title)
        return vh
    }

    private fun<T> 创建漫画内容持有者(parent: ViewGroup, bookType: BookType, viewType: Int): ComicBodyViewHolder {
        val vh = ComicBodyViewHolder(HomeFragmentComicRvBinding.inflate(from(parent.context), parent, false))
        vh.doComicNotify<T>(bookType, viewType)
        return vh
    }

    private fun 创建推荐换一批按钮(parent: ViewGroup) : ComicRecRefreshViewHolder {
        return ComicRecRefreshViewHolder(HomeFragmentComicRvRecRefreshBinding.inflate(from(parent.context), parent, false)).also { vh ->
            vh.rvBinding.root.animateFadeIn()
        }
    }

    private fun<T> HomeComicParentRvAdapter.ComicBodyViewHolder.doComicNotify(bookType: BookType, pos: Int, delay: Long = mRvDelayMs) {
        val adapter = HomeComicChildRvAdapter<T>(mBookType = bookType) { doOnTap(it) }
        if (bookType == BookType.Rec) mHomeRecComicRvAdapter = adapter as HomeComicChildRvAdapter<RecComicsResult>
        viewLifecycleOwner.lifecycleScope.launch {
            rvBinding.homeComicRv.adapter = adapter
            adapter.doNotify((mData!![pos] as MutableList<T>), delay)
            rvBinding.root.animateFadeIn()
        }
    }

    private fun HomeComicParentRvAdapter.BannerViewHolder.doBannerNotify(pos: Int) {
        rvBinding.homeBannerRv.isAutoPlay = false
        val adapter = HomeBannerRvAdapter { _, pathword -> doOnTap(BookTapEntity(Comic, pathword)) }
        viewLifecycleOwner.lifecycleScope.launch {
            rvBinding.homeBannerRv.adapter = adapter
            adapter.doBannerNotify((mData!![pos] as MutableList<Banner>), 0L)
            rvBinding.root.animateFadeIn()
        }
    }

    private fun HomeComicParentRvAdapter.ComicHeaderViewHolder.doInitComicHeader(@DrawableRes iconRes: Int, @StringRes text: Int) {
        rvBinding.homeComicButtonTitle.icon = ContextCompat.getDrawable(appContext, iconRes)
        rvBinding.homeComicButtonTitle.text = appContext.getString(text)
    }

    fun tryClearAndNotify() {
        val count = itemCount
        mData?.clear()
        notifyItemRangeRemoved(0, count)
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
    }

    suspend fun doRecNotify(datas: MutableList<RecComicsResult>, notifyMs: Long = 20L) {
        mHomeRecComicRvAdapter?.doNotify(datas, notifyMs)
    }

}
