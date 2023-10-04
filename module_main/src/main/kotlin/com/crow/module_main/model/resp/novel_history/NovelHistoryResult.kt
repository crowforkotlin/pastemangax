package com.crow.module_main.model.resp.novel_history


import com.squareup.moshi.Json

data class NovelHistoryResult(

    @Json(name = "book")
    val mBook: Book,

    @Json(name = "last_chapter_id")
    val mLastChapterId: String,

    @Json(name = "last_chapter_name")
    val mLastChapterName: String
)