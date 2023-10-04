package com.crow.module_main.model.resp.comic_history


import com.squareup.moshi.Json

data class ComicHistoryResult(

    @Json(name = "comic")
    val mComic: Comic,

    @Json(name = "id")
    val mID: Int,

    @Json(name = "last_chapter_id")
    val mLastChapterId: String,

    @Json(name = "last_chapter_name")
    val mLastChapterName: String
)