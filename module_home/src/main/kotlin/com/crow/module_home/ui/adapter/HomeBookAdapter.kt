@file:Suppress("UNCHECKED_CAST")

package com.crow.module_home.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.crow.base.app.appContext
import com.crow.base.extensions.clickGap
import com.crow.base.view.ToolTipsView
import com.crow.module_home.databinding.HomeRvBookLayoutBinding
import com.crow.module_home.model.ComicType
import com.crow.module_home.model.resp.homepage.*
import com.crow.module_home.model.resp.homepage.results.AuthorResult
import com.crow.module_home.model.resp.homepage.results.RecComicsResult
import com.crow.module_home.ui.fragment.HomeFragment
import java.text.DecimalFormat
import java.util.*

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_home/src/main/kotlin/com/crow/module_home/ui/adapter
 * @Time: 2023/3/11 2:17
 * @Author: CrowForKotlin
 * @Description: HomeBookAdapter
 * @formatter:on
 **************************/

private val formatter  = DecimalFormat.getInstance(Locale.US) as DecimalFormat

class HomeBookAdapter<T>(
    private var mData: T? = null,
    private val mType: ComicType,
    private val mClickComicListener: HomeFragment.ClickComicListener?
) :
    RecyclerView.Adapter<HomeBookAdapter<T>.ViewHolder>() {

    private val mCardHeight: Int = run {
        val width = appContext.resources.displayMetrics.widthPixels
        val height = appContext.resources.displayMetrics.heightPixels
        (width.toFloat() / (3 - width.toFloat() / height.toFloat())).toInt()
    }

    private var mSize: Int = 0

    private var mRootHeight: Int? = null

    inner class ViewHolder(val rvBinding: HomeRvBookLayoutBinding) : RecyclerView.ViewHolder(rvBinding.root)

    fun setData(value: T, size: Int? = null) {
        mData = value
        if (size != null) this.mSize = size
    }

    fun getUpdateSize() = mSize

    fun formatValue(value: Int): String {
        return when {
            value >= 10000 -> {
                formatter.applyPattern("#,#### W")
                formatter.format(value)
            }
            value >= 1000 -> {
                formatter.applyPattern("#,### K")
                formatter.format(value)
            }
            else -> value.toString()
        }
    }
    private fun ViewHolder.initView(name: String, imageUrl: String, author: List<AuthorResult>, hot: Int) {
        Glide.with(itemView).load(imageUrl).into(rvBinding.homeBookImage)
        rvBinding.homeBookName.text = name
        rvBinding.homeBookAuthor.text = author.joinToString { it.name }
        rvBinding.homeBookHot.text = formatValue(hot)
    }

    override fun getItemCount(): Int = if (mData == null) 0 else mSize
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(HomeRvBookLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)).also { vh ->
            vh.rvBinding.root.doOnLayout {
                vh.rvBinding.homeBookImage.layoutParams.height = mCardHeight
                mRootHeight = mRootHeight ?: it.height
                it.layoutParams.height = mRootHeight!!
            }

            vh.rvBinding.root.clickGap { _, _ -> }
            vh.rvBinding.root.clickGap { _, _ -> mClickComicListener?.onClick(mType) }
            vh.rvBinding.homeBookName.clickGap { _, _ -> }
            vh.rvBinding.homeBookCard.clickGap { _, _ -> }

            ToolTipsView.showToolTipsByLongClick(vh.rvBinding.homeBookName)
        }
    }
    override fun onBindViewHolder(vh: ViewHolder, pos: Int) {

        when (mType) {
            ComicType.Rec -> {
                val comic = (mData as ComicDatas<RecComicsResult>).mResult[pos].mComic
                vh.initView(comic.mName, comic.mImageUrl, comic.mAuthorResult, comic.mPopular)
            }
            ComicType.Hot -> {
                val comic = (mData as List<HotComic>)[pos].mComic
                vh.initView(comic.mName, comic.mImageUrl, comic.mAuthorResult, comic.mPopular)
            }
            ComicType.New -> {
                val comic = (mData as List<NewComic>)[pos].mComic
                vh.initView(comic.mName, comic.mImageUrl, comic.mAuthorResult, comic.mPopular)
            }
            ComicType.Commit -> {
                val comic = (mData as FinishComicDatas).mResult[pos]
                vh.initView(comic.mName, comic.mImageUrl, comic.mAuthorResult, comic.mPopular)
            }
            ComicType.Topic -> {
                val comic = (mData as ComicDatas<Topices>).mResult[pos]
                Glide.with(vh.itemView).load(comic.mImageUrl).into(vh.rvBinding.homeBookImage)
                vh.rvBinding.apply {
                    homeBookName.maxLines = 4
                    homeBookName.text = comic.mTitle
                    homeBookAuthor.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                    homeBookAuthor.text = comic.mDatetimeCreated
                    (homeBookAuthor.layoutParams as ConstraintLayout.LayoutParams).apply {
                        bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                    }
                    homeBookHot.visibility = View.GONE
                }
            }
            ComicType.Rank -> {
                val comic = (mData as ComicDatas<RankComics>).mResult[pos].mComic
                vh.initView(comic.mName, comic.mImageUrl, comic.mAuthorResult, comic.mPopular)
            }
        }
    }
}