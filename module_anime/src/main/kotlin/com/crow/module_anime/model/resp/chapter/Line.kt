package com.crow.module_anime.model.resp.chapter


import com.squareup.moshi.Json

data class Line(

    @Json(name = "config")
    val mConfig: Boolean,

    @Json(name = "name")
    val mName: String,

    @Json(name = "path_word")
    val mPathWord: String
)