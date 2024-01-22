package com.crow.module_book.model.resp.comic_page

import com.squareup.moshi.Json
import com.squareup.moshi.JsonQualifier


data class Content(

    @Json(ignore = true)
    var mID: Int = 0,

    @Json(ignore = true)
    var mPos: Int = 0,

    @Json(name =  "url")
    val mImageUrl: String,
)