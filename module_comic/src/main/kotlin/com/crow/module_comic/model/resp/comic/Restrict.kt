package com.crow.module_comic.model.resp.comic


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Restrict(
    @Json(name = "display")
    val display: String,
    @Json(name = "value")
    val value: Int
)