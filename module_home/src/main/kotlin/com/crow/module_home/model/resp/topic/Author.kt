package com.crow.module_home.model.resp.topic


import com.squareup.moshi.Json

data class Author(

    @Json(name = "name")
    val mName: String,

    @Json(name = "path_word")
    val mPathWord: String
)