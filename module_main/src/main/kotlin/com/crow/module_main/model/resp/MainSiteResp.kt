package com.crow.module_main.model.resp


import com.crow.module_main.model.resp.site.Site
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Main site resp
 *
 * @property mSiteList 站点列表
 * @constructor Create empty Main site resp
 */

@Serializable
data class MainSiteResp(

    @SerialName(value = "webList  ")
    val mSiteList: List<Site?>?
)