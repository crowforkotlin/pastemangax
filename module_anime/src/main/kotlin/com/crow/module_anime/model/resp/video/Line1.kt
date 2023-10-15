package com.crow.module_anime.model.resp.video


import com.squareup.moshi.Json

data class Line1(

    @Json(name = "config")
    val mConfig: Boolean,

    @Json(name = "name")
    val mName: String,

    @Json(name = "path_word")
    val mPathWord: String
)