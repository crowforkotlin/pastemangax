package com.crow.module_discover.model.resp.comic_home


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Theme(

    @Json(name = "name")
    val mName: String,

    @Json(name = "path_word")
    val mPathWord: String
)