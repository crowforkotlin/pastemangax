package com.crow.module_anime.model.resp.search


import com.squareup.moshi.Json

data class Company(

    @Json(name = "alias")
    val mAlias: String?,

    @Json(name = "name")
    val mName: String,

    @Json(name = "path_word")
    val mPathWord: String
)