package com.crow.module_book.model.resp.comic_browser

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class Browse(
    @SerialName(value = "chapter_id")
    val chapterId: String,
    @SerialName(value = "chapter_name")
    val chapterName: String,
    @SerialName(value = "chapter_uuid")
    val chapterUuid: String,
    @SerialName(value = "comic_id")
    val comicId: String,
    @SerialName(value = "comic_uuid")
    val comicUuid: String,
    @SerialName(value = "path_word")
    val pathWord: String
)