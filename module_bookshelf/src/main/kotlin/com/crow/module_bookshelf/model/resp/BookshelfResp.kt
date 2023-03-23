package com.crow.module_bookshelf.model.resp


import com.crow.module_bookshelf.model.resp.book_shelf.BookshelfResults
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BookshelfResp(

    @Json(name = "limit")
    val mLimit: Int,

    @Json(name = "list")
    val mList: List<BookshelfResults>,

    @Json(name = "offset")
    val mOffset: Int,

    @Json(name = "total")
    val mTotal: Int,
)