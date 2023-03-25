package com.crow.module_bookshelf.model.resp


import com.crow.module_bookshelf.model.resp.bookshelf_comic.BookshelfComicResults
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BookshelfComicfResp(

    @Json(name = "limit")
    val mLimit: Int,

    @Json(name = "list")
    val mList: List<BookshelfComicResults>,

    @Json(name = "offset")
    val mOffset: Int,

    @Json(name = "total")
    val mTotal: Int,
)