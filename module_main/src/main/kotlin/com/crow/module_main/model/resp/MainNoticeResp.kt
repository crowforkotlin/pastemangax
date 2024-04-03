package com.crow.module_main.model.resp

import com.squareup.moshi.Json

data class MainNoticeResp(
    @Json(name = "author")
    val mAuthor: String,

    @Json(name = "content")
    val mContent: String,

    @Json(name = "time")
    val mTime: String,

    @Json(name = "force_time")
    val mForceTime: Int,

    @Json(name = "version")
    val mVersion: Long,

    @Json(name = "force_content")
    val mForceContent: String
)