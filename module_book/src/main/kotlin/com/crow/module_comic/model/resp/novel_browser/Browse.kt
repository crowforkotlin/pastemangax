package com.crow.module_comic.model.resp.novel_browser


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Browse(
    @Json(name = "book_id")
    val bookId: String,
    @Json(name = "chapter_id")
    val chapterId: String,
    @Json(name = "chapter_name")
    val chapterName: String,
    @Json(name = "path_word")
    val pathWord: String
)