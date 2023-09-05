package com.crow.module_home.model.resp.homepage

import com.crow.module_home.model.resp.homepage.results.SeriesResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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

@Serializable
data class Topices(

    @SerialName(value = "title")
    val mTitle: String,

    @SerialName(value = "series")
    val mSeries: SeriesResult,

    @SerialName(value = "journal")
    val mJournal: String,

    @SerialName(value = "cover")
    val mImageUrl: String,

    @SerialName(value = "period")
    val mPeriod: String,

    @SerialName(value = "type")
    val mType: Int,

    @SerialName(value = "brief")
    val mBrief: String,

    @SerialName(value = "path_word")
    val mPathWord: String,

    @SerialName(value = "datetime_created")
    val mDatetimeCreated: String,
)