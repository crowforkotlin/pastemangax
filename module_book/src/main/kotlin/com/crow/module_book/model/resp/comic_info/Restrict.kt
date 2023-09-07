package com.crow.module_book.model.resp.comic_info

import com.squareup.moshi.Json


data class Restrict(

    @Json(name =  "display")
    val mDisplay: String,

    @Json(name =  "value")
    val mValue: Int
)