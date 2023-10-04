package com.crow.module_main.model.resp.comic_history


import com.squareup.moshi.Json

data class ComicHistoryResp(

    @Json(name = "limit")
    val mLimit: Int,

    @Json(name = "list")
    val mList: List<ComicHistoryResult>,

    @Json(name = "offset")
    val mOffset: Int,

    @Json(name = "total")
    val mTotal: Int
)