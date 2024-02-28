package com.crow.module_book.model.resp.comic_page

import com.squareup.moshi.Json


data class Content(

    @Json(ignore = true)
    var mChapterID: Int = 0,

    @Json(ignore = true)
    var mChapterPagePos: Int = 0,

    @Json(name =  "url")
    val mImageUrl: String,
)