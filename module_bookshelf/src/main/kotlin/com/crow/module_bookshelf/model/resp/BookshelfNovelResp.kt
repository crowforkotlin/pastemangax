package com.crow.module_bookshelf.model.resp


import com.crow.module_bookshelf.model.resp.bookshelf_novel.BookshelfNovelResults
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BookshelfNovelResp(

    @Json(name = "limit")
    val mLimit: Int,

    @Json(name = "list")
    val mList: List<BookshelfNovelResults>,

    @Json(name = "offset")
    val mOffset: Int,

    @Json(name = "total")
    val mTotal: Int
)