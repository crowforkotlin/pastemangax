package com.crow.module_book.model.resp.comic_chapter

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ComicChapterResult(
    @SerialName(value = "comic_id")
    val comicId: String,

    @SerialName(value = "comic_path_word")
    val comicPathWord: String,

    @SerialName(value = "count")
    val count: Int,

    @SerialName(value = "datetime_created")
    val datetimeCreated: String,

    @SerialName(value = "group_id")
    val groupId: @Polymorphic Any?,

    @SerialName(value = "group_path_word")
    val groupPathWord: String,

    @SerialName(value = "img_type")
    val imgType: Int,

    @SerialName(value = "index")
    val index: Int,

    @SerialName(value = "name")
    val name: String,

    @SerialName(value = "news")
    val news: String,

    @SerialName(value = "next")
    val next: String?,

    @SerialName(value = "ordered")
    val ordered: Int,

    @SerialName(value = "prev")
    val prev: String?,

    @SerialName(value = "size")
    val size: Int,

    @SerialName(value = "type")
    val type: Int,

    @SerialName(value = "uuid")
    val uuid: String,
)