package com.crow.module_book.model.resp.novel_browser


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Browse(
    @SerialName(value = "book_id")
    val bookId: String,
    @SerialName(value = "chapter_id")
    val chapterId: String,
    @SerialName(value = "chapter_name")
    val chapterName: String,
    @SerialName(value = "path_word")
    val pathWord: String
)