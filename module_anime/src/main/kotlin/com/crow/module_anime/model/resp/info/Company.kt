package com.crow.module_anime.model.resp.info


import com.squareup.moshi.Json

data class Company(

    @Json(name = "name")
    val mName: String,

    @Json(name = "path_word")
    val mPathWord: String
)