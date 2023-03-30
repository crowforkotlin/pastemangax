package com.crow.module_discover.model.resp.novel_tag


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Ordering(
    @Json(name = "name")
    val name: String,
    @Json(name = "path_word")
    val pathWord: String
)