package com.crow.module_anime.model.resp.video


import com.squareup.moshi.Json

data class Cartoon(

    @Json(name = "name")
    val mName: String,

    @Json(name = "path_word")
    val mPathWord: String,

    @Json(name = "uuid")
    val mUUID: String
)