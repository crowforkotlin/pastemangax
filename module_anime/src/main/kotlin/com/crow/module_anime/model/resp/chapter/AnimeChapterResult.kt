package com.crow.module_anime.model.resp.chapter


import com.squareup.moshi.Json

data class AnimeChapterResult(

    @Json(name = "lines")
    val mLines: List<Line>,

    @Json(name = "name")
    val mName: String,

    @Json(name = "uuid")
    val mUUID: String,

    @Json(name = "v_cover")
    val mVCover: String
)