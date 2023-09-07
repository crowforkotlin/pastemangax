package com.crow.module_book.model.resp.comic_browser

import com.squareup.moshi.Json


data class Browse(
    @Json(name =  "chapter_id")
    val chapterId: String,
    @Json(name =  "chapter_name")
    val chapterName: String,
    @Json(name =  "chapter_uuid")
    val chapterUuid: String,
    @Json(name =  "comic_id")
    val comicId: String,
    @Json(name =  "comic_uuid")
    val comicUuid: String,
    @Json(name =  "path_word")
    val pathWord: String
)