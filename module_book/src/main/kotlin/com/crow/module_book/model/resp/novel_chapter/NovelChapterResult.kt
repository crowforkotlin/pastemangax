package com.crow.module_book.model.resp.novel_chapter


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NovelChapterResult (
    @Json(name = "book_id")
    val bookId: String,
    @Json(name = "book_path_word")
    val bookPathWord: String,
    @Json(name = "count")
    val count: Int,
    @Json(name = "id")
    val id: String,
    @Json(name = "index")
    val index: Int,
    @Json(name = "name")
    val name: String,
    @Json(name = "next")
    val next: String?,
    @Json(name = "prev")
    val prev: String?,
    @Json(name = "sort")
    val sort: Int
)