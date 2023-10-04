package com.crow.module_main.model.resp.novel_history


import com.squareup.moshi.Json

data class NovelHistoryResp(
    @Json(name = "limit")
    val mLimit: Int,

    @Json(name = "list")
    val mList: List<NovelHistoryResult>,

    @Json(name = "offset")
    val mOffset: Int,

    @Json(name = "total")
    val mTotal: Int
)