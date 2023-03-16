package com.crow.module_comic.model.resp.comic


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Comic(
    @Json(name = "name")
    val name: String,
    @Json(name = "path_word")
    val pathWord: String,
    @Json(name = "restrict")
    val restrict: Restrict,
    @Json(name = "uuid")
    val uuid: String
)