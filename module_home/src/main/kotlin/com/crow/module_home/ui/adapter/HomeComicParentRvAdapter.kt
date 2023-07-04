@file:Suppress("UNCHECKED_CAST", "FunctionName", "NonAsciiCharacters")

package com.crow.module_home.ui.adapter

import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.app.appContext
import com.crow.base.tools.extensions.BASE_ANIM_100L
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.tools.extensions.px2dp
import com.crow.module_home.R
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
import com.crow.base.R as baseR
import com.to.aboomy.pager2banner.Banner as BannerView

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
    val doOnTap: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class Type(val POSIITON: Int) {
        REC(2), HOT(5), NEW(7), FINISH(9), RANK(11), TOPIC(13)
    }

    inner class HomeComicParentViewHolder<T : ViewGroup>(view: T) : RecyclerView.ViewHolder(view) {
        var mPathWord: String = ""
    }

    private var mHomeRecComicRvAdapter: HomeComicChildRvAdapter<RecComicsResult>? = null

    private var mRvDelayMs: Long = 20L
    private val mDp5 by lazy { appContext.resources.getDimensionPixelSize(baseR.dimen.base_dp5) }
    private val mDp10 by lazy { appContext.resources.getDimensionPixelSize(baseR.dimen.base_dp10) }
    private val mDp20 by lazy { appContext.resources.getDimensionPixelSize(baseR.dimen.base_dp20) }

    override fun getItemCount(): Int = mData?.size ?: 0

    override fun getItemViewType(position: Int) = position

    override fun onBindViewHolder(vh: RecyclerView.ViewHolder, pos: Int) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> createBannerVH(parent)
            1 -> createHeaderVH(
                parent,
                R.drawable.home_ic_recommed_24dp,
                R.string.home_recommend_comic
            )
            2 -> createComicVH<RecComicsResult>(parent, Type.REC,)
            3 -> createRefreshButtonVH(parent)
            4 -> createHeaderVH(parent, R.drawable.home_ic_hot_24dp, R.string.home_hot_comic)
            5 -> createComicVH<HotComic>(parent, Type.HOT)
            6 -> createHeaderVH(parent, R.drawable.home_ic_new_24dp, R.string.home_new_comic)
            7 -> createComicVH<NewComic>(parent, Type.NEW)
            8 -> createHeaderVH(parent, R.drawable.home_ic_finish_24dp, R.string.home_commit_finish)
            9 -> createComicVH<FinishComic>(parent, Type.FINISH)
            10 -> createHeaderVH(parent, R.drawable.home_ic_rank_24dp, R.string.home_rank_comic)
            11 -> createComicVH<RankComics>(parent, Type.RANK)
            12 -> createHeaderVH(parent, R.drawable.home_ic_topic_24dp, R.string.home_topic_comic)
            else -> createComicVH<Topices>(parent, Type.TOPIC)
        }
    }

    private fun createBannerVH(parent: ViewGroup): HomeComicParentViewHolder<BannerView> {
        val banner = BannerView(parent.context)
        banner.layoutParams = RelativeLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        (banner.layoutParams as RelativeLayout.LayoutParams).topMargin = mDp10
        banner.isNestedScrollingEnabled = false
        banner.doOnLayout { it.layoutParams.height = (it.width / 1.875 + 0.5).toInt() }
        banner.addPageTransformer(ScaleInTransformer())
            .setPageMargin(mDp20, mDp10)
            .setIndicator(
                IndicatorView(parent.context)
                    .setIndicatorColor(Color.DKGRAY)
                    .setIndicatorSelectorColor(Color.WHITE)
                    .setIndicatorStyle(IndicatorView.IndicatorStyle.INDICATOR_BEZIER)
                    .also { it.setPadding(0, 0, 0, mDp20) })
        val adapter =HomeBannerRvAdapter { pathword -> doOnTap(pathword) }
        banner.adapter = adapter
        adapter.doBannerNotify(mData!![0] as MutableList<Banner>, mRvDelayMs / 2, viewLifecycleOwner)
        return HomeComicParentViewHolder(banner)
    }

    private fun createHeaderVH(
        parent: ViewGroup,
        @DrawableRes icon: Int,
        @StringRes title: Int
    ): RecyclerView.ViewHolder {
        val titleLinear = LinearLayoutCompat(parent.context)
        val titleButton = MaterialButton(parent.context, null, baseR.attr.baseIconButtonStyle)
        val titleMore = MaterialButton(parent.context, null, baseR.attr.baseIconButtonStyle)
        titleLinear.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        titleLinear.orientation = LinearLayoutCompat.HORIZONTAL
        titleButton.layoutParams = LinearLayoutCompat.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).also { it.weight = 1f }
        titleButton.isClickable = false
        titleButton.gravity = Gravity.CENTER_VERTICAL
        titleButton.textSize = parent.context.px2dp(appContext.resources.getDimension(baseR.dimen.base_sp18))
        titleButton.text = parent.context.getString(title)
        titleButton.setTypeface(titleButton.typeface, Typeface.BOLD)
        titleButton.icon = ContextCompat.getDrawable(parent.context, icon)
        titleButton.iconSize = appContext.resources.getDimensionPixelSize(baseR.dimen.base_dp48)
        titleButton.iconTint = null
        titleButton.iconPadding = appContext.resources.getDimensionPixelSize(baseR.dimen.base_dp12)
        titleMore.isClickable = false
        titleMore.gravity = Gravity.CENTER_VERTICAL
        titleMore.icon = ContextCompat.getDrawable(appContext, baseR.drawable.base_ic_more_24dp)
        titleMore.iconTint = ContextCompat.getColorStateList(parent.context, baseR.color.base_light_blue_500)
        titleLinear.addView(titleButton)
        titleLinear.addView(titleMore)
        return HomeComicParentViewHolder(titleLinear)
    }

    private fun <T> createComicVH(
        parent: ViewGroup,
        type: Type,
    ): HomeComicParentViewHolder<RecyclerView> {
        val recyclerView = RecyclerView(parent.context)
        recyclerView.layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        (recyclerView.layoutParams as RecyclerView.LayoutParams).setMargins(mDp5, mDp5, mDp5, mDp5)
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.layoutManager = GridLayoutManager(parent.context, if (type != Type.TOPIC) 3 else 2)
        val adapter = HomeComicChildRvAdapter<T>(mType = type, doOnTap = { doOnTap(it) })
        recyclerView.adapter = adapter
        when (type) {
            Type.REC -> {
                mHomeRecComicRvAdapter = recyclerView.adapter as HomeComicChildRvAdapter<RecComicsResult>
                mHomeRecComicRvAdapter?.doNotify((mData!![Type.REC.POSIITON] as  MutableList<RecComicsResult>), mRvDelayMs * 2, viewLifecycleOwner)
            }
            else -> {
                adapter.doNotify((mData!![type.POSIITON] as  MutableList<T>), mRvDelayMs * 2, viewLifecycleOwner)
            }
        }
        return HomeComicParentViewHolder(recyclerView)
    }

    private fun createRefreshButtonVH(parent: ViewGroup): HomeComicParentViewHolder<FrameLayout> {
        val frameLayout = FrameLayout(parent.context)
        val refreshButton =
            MaterialButton(parent.context, null, baseR.attr.baseIconButtonElevatedStyle)
        frameLayout.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        refreshButton.layoutParams = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        (refreshButton.layoutParams as FrameLayout.LayoutParams).apply {
            gravity = Gravity.END
            marginEnd = appContext.resources.getDimensionPixelSize(baseR.dimen.base_dp10)
        }
        refreshButton.text = parent.context.getString(R.string.home_refresh)
        refreshButton.setTypeface(refreshButton.typeface, Typeface.BOLD)
        refreshButton.textAlignment = View.TEXT_ALIGNMENT_CENTER
        refreshButton.elevation = 20f
        refreshButton.icon = ContextCompat.getDrawable(parent.context, R.drawable.home_ic_refresh_24dp)
        refreshButton.iconPadding = mDp5
        refreshButton.iconTint = null
        refreshButton.doOnClickInterval { _ -> doOnRecRefresh(refreshButton) }
        frameLayout.addView(refreshButton)
        return HomeComicParentViewHolder(frameLayout)
    }

    suspend fun doNotify(newDataResult: MutableList<Any?>, delayMs: Long = BASE_ANIM_100L) {
        val isCountSame = itemCount == newDataResult.size
        if (isCountSame) mData = newDataResult
        else if (itemCount == 0) {
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

    fun doRecNotify(datas: MutableList<RecComicsResult>, notifyMs: Long = mRvDelayMs) {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            mHomeRecComicRvAdapter?.doNotify(datas, notifyMs, viewLifecycleOwner)
        }
    }
}
