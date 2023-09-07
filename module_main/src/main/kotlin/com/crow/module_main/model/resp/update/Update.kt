package com.crow.module_main.model.resp.update

import com.squareup.moshi.Json


data class Update (

    @Json(name =  "update_content")
    val mContent: String,

    @Json(name =  "update_title")
    val mTitle: String,

    @Json(name =  "update_url")
    val mUrl: List<Url>,

    @Json(name =  "update_version_code")
    val mVersionCode: Int,

    @Json(name =  "update_version_name")
    val mVersionName: String,

    @Json(name =  "update_time")
    val mTime: String
)
