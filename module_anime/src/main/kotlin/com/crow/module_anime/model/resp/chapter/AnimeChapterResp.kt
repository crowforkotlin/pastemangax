package com.crow.module_anime.model.resp.chapter


import com.squareup.moshi.Json

data class AnimeChapterResp(

    @Json(name = "limit")
    val mLimit: Int,

    @Json(name = "list")
    val mList: List<AnimeChapterResult>,

    @Json(name = "offset")
    val mOffset: Int,

    @Json(name = "total")
    val mTotal: Int
)