package com.crow.module_home.model.resp.homepage

import com.crow.module_home.model.resp.homepage.results.ComicResultXX
import com.squareup.moshi.Json

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
data class RankComics(

    @Json(name = "sort")
    val mSort: Int,

    @Json(name = "sort_last")
    val mSortLast: Int,

    @Json(name = "rise_sort")
    val mRiseSort: Int,

    @Json(name = "rise_num")
    val mRiseNum: Int,

    @Json(name = "date_type")
    val mDateType: Int,

    @Json(name = "popular")
    val mPopular: Int,

    @Json(name = "comic")
    val mComic: ComicResultXX,
)
