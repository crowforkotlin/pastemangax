package com.crow.module_discover.model.resp.comic_home

import com.squareup.moshi.Json


data class FreeType(

    @Json(name =  "display")
    val mDisplay: String,

    @Json(name =  "value")
    val mValue: Int
)