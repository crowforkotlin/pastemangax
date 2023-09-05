package com.crow.module_book.model.resp


import com.crow.module_book.model.resp.comic_chapter.ComicChapterResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ComicChapterResp(

    @SerialName(value = "limit")
    val mLimit: Int,

    @SerialName(value = "list")
    val mList: List<ComicChapterResult>,

    @SerialName(value = "offset")
    val mOffset: Int,

    @SerialName(value = "total")
    val mTotal: Int,
)