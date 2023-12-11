package com.crow.module_book.model.resp.comic_chapter

import com.squareup.moshi.Json


data class ComicChapterResult(
    @Json(name =  "comic_id")
    val comicId: String,

    @Json(name =  "comic_path_word")
    val comicPathWord: String,

    @Json(name =  "count")
    val count: Int,

    @Json(name =  "datetime_created")
    val datetimeCreated: String,

    @Json(name =  "group_id")
    val groupId: Any?,

    @Json(name =  "group_path_word")
    val groupPathWord: String,

    @Json(name =  "img_type")
    val imgType: Int,

    @Json(name =  "index")
    val index: Int,

    @Json(name =  "name")
    val name: String,

    @Json(name =  "news")
    val news: String,

    @Json(name =  "next")
    val next: String?,

    @Json(name =  "ordered")
    val ordered: Int,

    @Json(name =  "prev")
    val prev: String?,

    @Json(name =  "size")
    val size: Int,

    @Json(name =  "type")
    val type: Int,

    @Json(name =  "uuid")
    val uuid: String,
)