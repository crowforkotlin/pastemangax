package com.crow.module_bookshelf.model.resp


import com.crow.module_bookshelf.model.resp.bookshelf_novel.BookshelfNovelResults
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BookshelfNovelResp(

    @SerialName(value = "limit")
    val mLimit: Int,

    @SerialName(value = "list")
    val mList: List<BookshelfNovelResults>,

    @SerialName(value = "offset")
    val mOffset: Int,

    @SerialName(value = "total")
    val mTotal: Int
)