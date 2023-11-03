package com.crow.module_home.model.resp.topic


import com.squareup.moshi.Json

data class TopicResp(

    @Json(name = "limit")
    val mLimit: Int,

    @Json(name = "list")
    val mList: List<TopicResult>,

    @Json(name = "offset")
    val mOffset: Int,

    @Json(name = "total")
    val mTotal: Int
)