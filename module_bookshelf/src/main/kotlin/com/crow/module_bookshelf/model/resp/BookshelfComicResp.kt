package com.crow.module_bookshelf.model.resp


import com.crow.module_bookshelf.model.resp.bookshelf_comic.BookshelfComicResults
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BookshelfComicResp(

    @SerialName(value = "limit")
    val mLimit: Int,

    @SerialName(value = "list")
    val mList: List<BookshelfComicResults>,

    @SerialName(value = "offset")
    val mOffset: Int,

    @SerialName(value = "total")
    val mTotal: Int,
)