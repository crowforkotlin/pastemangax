package com.crow.module_main.model.resp.site

import com.squareup.moshi.Json


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

data class Site(

    @Json(name =  "desc")
    val mDesc: String,

    @Json(name =  "level")
    val mLevel: Int,

    @Json(name =  "magic")
    val mNeedVPN: Boolean,

    @Json(name =  "name  ")
    val mName: String,

    @Json(name =  "site")
    val mEncodeSite: String,

    @Json(name =  "update_time")
    val mUpdateTime: String
)