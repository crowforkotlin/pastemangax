package com.crow.module_book.model.resp.comic_page


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Restrict(
    @Json(name = "display")
    val display: String,
    @Json(name = "value")
    val value: Int
)