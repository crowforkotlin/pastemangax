package com.crow.module_comic.model.resp.comic


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Chapter(
    @Json(name = "comic_id")
    val comicId: String,

    @Json(name = "comic_path_word")
    val comicPathWord: String,

    @Json(name = "contents")
    val contents: List<Content>,

    @Json(name = "count")
    val count: Int,

    @Json(name = "datetime_created")
    val datetimeCreated: String,

    @Json(name = "group_id")
    val groupId: Any?,

    @Json(name = "group_path_word")
    val groupPathWord: String,

    @Json(name = "img_type")
    val imgType: Int,

    @Json(name = "index")
    val index: Int,

    @Json(name = "is_long")
    val isLong: Boolean,

    @Json(name = "name")
    val name: String,

    @Json(name = "news")
    val news: String,

    @Json(name = "next")
    val next: String,

    @Json(name = "ordered")
    val ordered: Int,

    @Json(name = "prev")
    val prev: Any?,

    @Json(name = "size")
    val size: Int,

    @Json(name = "type")
    val type: Int,

    @Json(name = "uuid")
    val uuid: String,

    @Json(name = "words")
    val words: List<Int>
)