package com.crow.module_book.model.resp.novel_chapter

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NovelChapterResult (
    @SerialName(value = "book_id")
    val bookId: String,
    @SerialName(value = "book_path_word")
    val bookPathWord: String,
    @SerialName(value = "count")
    val count: Int,
    @SerialName(value = "id")
    val id: String,
    @SerialName(value = "index")
    val index: Int,
    @SerialName(value = "name")
    val name: String,
    @SerialName(value = "next")
    val next: String?,
    @SerialName(value = "prev")
    val prev: String?,
    @SerialName(value = "sort")
    val sort: Int
)