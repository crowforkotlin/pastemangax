package com.crow.module_home.model.resp.homepage

import com.crow.module_home.model.resp.homepage.results.PathwordResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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

@Serializable
data class Banner(

    @SerialName(value = "brief")
    val mBrief: String,

    @SerialName(value = "comic")
    val mComic: PathwordResult?,

    @SerialName(value = "cover")
    val mImgUrl: String,

    @SerialName(value = "out_uuid")
    val mOutUuid: String,

    @SerialName(value = "type")
    val mType: Int,
)