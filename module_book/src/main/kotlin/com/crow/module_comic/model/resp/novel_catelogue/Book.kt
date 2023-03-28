package com.crow.module_comic.model.resp.novel_catelogue


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Book(
    @Json(name = "name")
    val name: String,
    @Json(name = "path_word")
    val pathWord: String,
    @Json(name = "uuid")
    val uuid: String
)