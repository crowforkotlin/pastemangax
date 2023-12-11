package com.crow.module_anime.model.resp.info


import com.squareup.moshi.Json

data class Grade(

    @Json(name = "display")
    val mDisplay: String,

    @Json(name = "value")
    val mValue: Int
)