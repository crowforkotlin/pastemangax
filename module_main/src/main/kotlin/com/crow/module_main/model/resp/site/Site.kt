package com.crow.module_main.model.resp.site

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Site
 *
 * @property mDesc 介绍
 * @property mLevel 0 < 1
 * @property mNeedVPN 魔法
 * @property mName 站点名称
 * @property mEncodeSite 加密站点 需要Base64解码
 * @property mUpdateTime 更新日期
 * @constructor Create empty Site
 */

@Serializable
data class Site(

    @SerialName(value = "desc")
    val mDesc: String,

    @SerialName(value = "level")
    val mLevel: Int,

    @SerialName(value = "magic")
    val mNeedVPN: Boolean,

    @SerialName(value = "name  ")
    val mName: String,

    @SerialName(value = "site")
    val mEncodeSite: String,

    @SerialName(value = "update_time")
    val mUpdateTime: String
)