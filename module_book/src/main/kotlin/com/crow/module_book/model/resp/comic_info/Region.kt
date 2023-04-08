package com.crow.module_book.model.resp.comic_info


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Region(

    @Json(name = "display")
    val mDisplay: String,

    @Json(name = "value")
    val mValue: Int
)