package com.crow.module_home.model.resp.homepage

import com.crow.module_home.model.resp.homepage.results.SeriesResult
import com.squareup.moshi.Json

/**
 * Topices
 *
 * @property mTitle 标题
 * @property mSeries 系列
 * @property mJournal 杂志年份
 * @property mImageUrl 图片路径
 * @property mPeriod 杂志（第几期）
 * @property mType 类型
 * @property mBrief 备注信息
 * @property mPathWord 路径词
 * @property mDatetimeCreated 创建日期
 * @constructor Create empty Topices
 */
data class Topices(

    @Json(name = "title")
    val mTitle: String,

    @Json(name = "series")
    val mSeries: SeriesResult,

    @Json(name = "journal")
    val mJournal: String,

    @Json(name = "cover")
    val mImageUrl: String,

    @Json(name = "period")
    val mPeriod: String,

    @Json(name = "type")
    val mType: Int,

    @Json(name = "brief")
    val mBrief: String,

    @Json(name = "path_word")
    val mPathWord: String,

    @Json(name = "datetime_created")
    val mDatetimeCreated: String,
)