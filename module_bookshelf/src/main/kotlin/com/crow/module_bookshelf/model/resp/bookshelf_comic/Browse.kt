package com.crow.module_bookshelf.model.resp.bookshelf_comic

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Browse(
    @SerialName(value = "chapter_name")
    val mChapterName: String,

    @SerialName(value = "chapter_uuid")
    val mChapterUuid: String,

    @SerialName(value = "comic_uuid")
    val mComicUuid: String,

    @SerialName(value = "path_word")
    val mPathWord: String
)