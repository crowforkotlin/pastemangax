package com.crow.module_comic.model.resp.comic_info

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Default(

    @Json(name = "count")
    val mCount: Int,

    @Json(name = "name")
    val mName: String,

    @Json(name = "path_word")
    val mPathWord: String,
)