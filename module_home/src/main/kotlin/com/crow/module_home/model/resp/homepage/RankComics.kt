package com.crow.module_home.model.resp.homepage

import com.crow.module_home.model.resp.homepage.results.ComicResultXX
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Rank comics
 *
 * @property mSort 排序编号
 * @property mSortLast
 * @property mRiseSort
 * @property mRiseNum 上升的排名
 * @property mDateType 日期类型
 * @property mPopular 热度
 * @property mComic 漫画集
 * @constructor Create empty Rank comics
 */

@Serializable
data class RankComics(

    @SerialName(value = "sort")
    val mSort: Int,

    @SerialName(value = "sort_last")
    val mSortLast: Int,

    @SerialName(value = "rise_sort")
    val mRiseSort: Int,

    @SerialName(value = "rise_num")
    val mRiseNum: Int,

    @SerialName(value = "date_type")
    val mDateType: Int,

    @SerialName(value = "popular")
    val mPopular: Int,

    @SerialName(value = "comic")
    val mComic: ComicResultXX,
)
