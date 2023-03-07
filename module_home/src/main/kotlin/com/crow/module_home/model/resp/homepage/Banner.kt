package com.crow.module_home.model.resp.homepage

import com.crow.module_home.model.resp.homepage.results.PathwordResult
import com.squareup.moshi.Json

/**
 * Banner
 *
 * @property mBrief 备注信息（可做标题）
 * @property mComic
 * @property mImgUrl 图片URL
 * @property mOutUuid 路由参数
 * @property mType 类型 （1 ：小说） （3 和 4 为 工具和广告）
 * @constructor Create empty Banner
 */
data class Banner(

    @Json(name = "brief")
    val mBrief: String,

    @Json(name = "comic")
    val mComic: PathwordResult?,

    @Json(name = "cover")
    val mImgUrl: String,

    @Json(name = "out_uuid")
    val mOutUuid: String,

    @Json(name = "type")
    val mType: Int,
)