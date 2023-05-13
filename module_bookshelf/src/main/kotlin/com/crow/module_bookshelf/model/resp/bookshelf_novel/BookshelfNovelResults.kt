package com.crow.module_bookshelf.model.resp.bookshelf_novel


import com.crow.module_bookshelf.model.resp.bookshelf.LastBrowse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BookshelfNovelResults (

    @Json(name = "book")
    val mNovel: Novel,

    @Json(name = "last_browse")
    val mLastBrowse: LastBrowse?,

    @Json(name = "uuid")
    val mUuid: Int
)