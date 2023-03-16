package com.crow.module_comic.model.resp


import com.crow.module_comic.model.resp.comic_chapter.Results
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ComicChapterResp(

    @Json(name = "code")
    val code: Int,

    @Json(name = "message")
    val message: String,

    @Json(name = "results")
    val results: Results
)