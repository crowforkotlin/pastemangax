@file:Suppress("UNCHECKED_CAST")

package com.crow.module_home.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater.from
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.R
import com.crow.base.app.appContext
import com.crow.base.current_project.entity.BookType
import com.crow.module_home.databinding.HomeFragmentBanner2Binding
import com.crow.module_home.databinding.HomeFragmentComicRv2Binding
import com.crow.module_home.model.resp.homepage.*
import com.crow.module_home.model.resp.homepage.results.RecComicsResult
import com.to.aboomy.pager2banner.IndicatorView
import com.to.aboomy.pager2banner.ScaleInTransformer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_home/src/main/kotlin/com/crow/module_home/ui/adapter
 * @Time: 2023/4/3 2:50
 * @Author: CrowForKotlin
 * @Description: HomeBookAdapter
 * @formatter:on
 **************************/

class HomeComicRvAdapter2(
    private var mData: MutableList<Any>? = null,
    private val viewLifecycleOwner: LifecycleOwner,
    inline val doOnTap: (BookType, String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class ComicViewHolder(val rvBinding: HomeFragmentComicRv2Binding) : RecyclerView.ViewHolder(rvBinding.root) { var mPathWord: String = "" }

    inner class BannerViewHolder(val rvBinding: HomeFragmentBanner2Binding) : RecyclerView.ViewHolder(rvBinding.root) { var mPathWord: String = "" }

    private lateinit var mHomeComicRvAdapter: HomeComicRvAdapter<Any>
    private lateinit var mHomeBannerRvAdapter: HomeBannerRvAdapter

    override fun getItemCount(): Int = mData?.size ?: 0

    override fun getItemViewType(position: Int) = position

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            0 -> BannerViewHolder(HomeFragmentBanner2Binding.inflate(from(parent.context), parent, false)).also { vh ->

            }
            else -> ComicViewHolder(HomeFragmentComicRv2Binding.inflate(from(parent.context), parent, false)).also { vh ->
                /*when (viewType) {
                    1 -> vh.doComicNotify<RecComicsResult>(BookType.Rec, viewType)
                    2 -> vh.doComicNotify<HotComic>(BookType.Hot, viewType)
                    3 -> vh.doComicNotify<NewComic>(BookType.New, viewType)
                    4 -> vh.doComicNotify<FinishComic>(BookType.Finish, viewType)
                    5 -> vh.doComicNotify<RankComics>(BookType.Rank, viewType)
                    6 -> {
                        vh.doComicNotify<Topices>(BookType.Topic, viewType)
                        vh.rvBinding.homeFragmentComicRv2.layoutManager = GridLayoutManager(vh.itemView.context, 2)
                    }
                }*/
            }
        }
    }

    private fun<T> HomeComicRvAdapter2.ComicViewHolder.doComicNotify(bookType: BookType, pos: Int, delay: Long = 100L) {
        viewLifecycleOwner.lifecycleScope.launch {
            val adapter = HomeComicRvAdapter3<T>(mBookType = bookType) { _, _ -> }
            rvBinding.homeFragmentComicRv2.adapter = adapter
            adapter.doNotify((mData!![pos] as MutableList<T>), delay)
        }
    }

    private fun HomeComicRvAdapter2.BannerViewHolder.doBannerNotify(pos: Int, delay: Long = 100L) {
        viewLifecycleOwner.lifecycleScope.launch {
            val adapter = HomeBannerRvAdapter { _, _ -> }
            rvBinding.homeFragmentBanner2.adapter = adapter
            adapter.doBannerNotify((mData!![pos] as MutableList<Banner>), delay)
        }
    }

    override fun onBindViewHolder(vh: RecyclerView.ViewHolder, pos: Int) {
        if (vh is HomeComicRvAdapter2.ComicViewHolder) {
            when (pos) {
                1 -> {
//                    vh.rvBinding.homeFragmentComicRv2.adapter = HomeComicRvAdapter3((mData!![pos] as MutableList<RecComicsResult>) ,mBookType = BookType.Rec) { _, _ -> }
                    vh.rvBinding.homeFragmentComicRv2.adapter = mData!![pos] as HomeComicRvAdapter3<RecComicsResult>
                }
                2 -> {
//                    vh.rvBinding.homeFragmentComicRv2.adapter = HomeComicRvAdapter3((mData!![pos] as MutableList<HotComic>) ,mBookType = BookType.Hot) { _, _ -> }
                    vh.rvBinding.homeFragmentComicRv2.adapter = mData!![pos] as HomeComicRvAdapter3<HotComic>
                }
                3 -> {
//                    vh.rvBinding.homeFragmentComicRv2.adapter = HomeComicRvAdapter3((mData!![pos] as MutableList<NewComic>) ,mBookType = BookType.New) { _, _ -> }
                    vh.rvBinding.homeFragmentComicRv2.adapter = mData!![pos] as HomeComicRvAdapter3<NewComic>
                }
                4 -> {
//                    vh.rvBinding.homeFragmentComicRv2.adapter = HomeComicRvAdapter3((mData!![pos] as MutableList<FinishComic>) ,mBookType = BookType.Finish) { _, _ -> }
                    vh.rvBinding.homeFragmentComicRv2.adapter = mData!![pos] as HomeComicRvAdapter3<FinishComic>
                }
                5 -> {
//                    vh.rvBinding.homeFragmentComicRv2.adapter = HomeComicRvAdapter3((mData!![pos] as MutableList<RankComics>) ,mBookType = BookType.Rank) { _, _ -> }
                    vh.rvBinding.homeFragmentComicRv2.adapter = mData!![pos] as HomeComicRvAdapter3<RankComics>
                }
                6 -> {
//                    vh.rvBinding.homeFragmentComicRv2.adapter = HomeComicRvAdapter3((mData!![pos] as MutableList<Topices>) ,mBookType = BookType.Topic) { _, _ -> }
                    vh.rvBinding.homeFragmentComicRv2.adapter = mData!![pos] as HomeComicRvAdapter3<Topices>
                }
                else -> { }
            }
            return
        }

        if (vh is BannerViewHolder) {
            val base20 = appContext.resources.getDimensionPixelSize(R.dimen.base_dp20)
            vh.rvBinding.homeFragmentBanner2.doOnLayout { it.layoutParams.height = (it.width / 1.875 + 0.5).toInt() }
            vh.rvBinding.homeFragmentBanner2.addPageTransformer(ScaleInTransformer())
                .setPageMargin(base20, appContext.resources.getDimensionPixelSize(R.dimen.base_dp10))
                .setIndicator(
                    IndicatorView(vh.itemView.context)
                        .setIndicatorColor(Color.DKGRAY)
                        .setIndicatorSelectorColor(Color.WHITE)
                        .setIndicatorStyle(IndicatorView.IndicatorStyle.INDICATOR_BEZIER)
                        .also { it.setPadding(0, 0, 0, base20) })
                .adapter = mData!![pos] as HomeBannerRvAdapter
        }
    }
}
