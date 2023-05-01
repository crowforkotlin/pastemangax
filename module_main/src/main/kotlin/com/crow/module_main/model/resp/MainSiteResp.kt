package com.crow.module_main.model.resp


import com.crow.module_main.model.resp.site.Site
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Main site resp
 *
 * @property mSiteList 站点列表
 * @constructor Create empty Main site resp
 */
@JsonClass(generateAdapter = true)
data class MainSiteResp(

    @Json(name = "webList  ")
    val mSiteList: List<Site?>?
)