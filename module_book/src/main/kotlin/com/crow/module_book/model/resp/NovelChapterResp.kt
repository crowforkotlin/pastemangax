package com.crow.module_book.model.resp


import com.crow.module_book.model.resp.novel_chapter.NovelChapterResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NovelChapterResp(
    @SerialName(value = "limit")
    val mLimit: Int,

    @SerialName(value = "list")
    val mList: List<NovelChapterResult>,

    @SerialName(value = "offset")
    val mOffset: Int,

    @SerialName(value = "total")
    val mTotal: Int
)