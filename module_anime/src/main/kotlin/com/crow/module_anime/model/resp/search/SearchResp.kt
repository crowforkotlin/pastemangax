package com.crow.module_anime.model.resp.search


import com.squareup.moshi.Json

data class SearchResp(

    @Json(name = "limit")
    val mLimit: Int,

    @Json(name = "list")
    val mList: List<SearchResult>,

    @Json(name = "offset")
    val mOffset: Int,

    @Json(name = "total")
    val mTotal: Int
)