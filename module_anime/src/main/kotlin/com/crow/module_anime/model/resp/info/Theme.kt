package com.crow.module_anime.model.resp.info


import com.squareup.moshi.Json

data class Theme(

    @Json(name = "name")
    val mName: String,

    @Json(name = "path_word")
    val mPathWord: String
)