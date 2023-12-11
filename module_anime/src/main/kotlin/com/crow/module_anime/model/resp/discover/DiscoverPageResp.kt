package com.crow.module_anime.model.resp.discover


import com.squareup.moshi.Json

data class DiscoverPageResp(

    @Json(name = "limit")
    val mLimit: Int,

    @Json(name = "list")
    val mList: List<DiscoverPageResult>,

    @Json(name = "offset")
    val mOffset: Int,

    @Json(name = "total")
    val mTotal: Int
)