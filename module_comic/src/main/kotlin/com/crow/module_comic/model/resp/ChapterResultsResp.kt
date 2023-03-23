package com.crow.module_comic.model.resp


import com.crow.module_comic.model.resp.comic_chapter.Comic
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChapterResultsResp(

    @Json(name = "limit")
    val limit: Int,

    @Json(name = "list")
    val list: List<Comic>,

    @Json(name = "offset")
    val offset: Int,

    @Json(name = "total")
    val total: Int,
)