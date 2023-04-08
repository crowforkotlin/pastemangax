package com.crow.module_book.model.resp.novel_info


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Region(
    @Json(name = "display")
    val display: String,
    @Json(name = "value")
    val value: Int
)