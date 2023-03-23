package com.crow.module_bookshelf.model.resp.book_shelf


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Browse(
    @Json(name = "chapter_name")
    val mChapterName: String,

    @Json(name = "chapter_uuid")
    val mChapterUuid: String,

    @Json(name = "comic_uuid")
    val mComicUuid: String,

    @Json(name = "path_word")
    val mPathWord: String
)