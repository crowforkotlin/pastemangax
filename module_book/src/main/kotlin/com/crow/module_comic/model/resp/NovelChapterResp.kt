package com.crow.module_comic.model.resp


import com.crow.module_comic.model.resp.novel_chapter.NovelChapterResult
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NovelChapterResp(
    @Json(name = "limit")
    val mLimit: Int,

    @Json(name = "list")
    val mList: List<NovelChapterResult>,

    @Json(name = "offset")
    val mOffset: Int,

    @Json(name = "total")
    val mTotal: Int
)