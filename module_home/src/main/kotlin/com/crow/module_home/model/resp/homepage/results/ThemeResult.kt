package com.crow.module_home.model.resp.homepage.results

import com.squareup.moshi.Json


data class ThemeResult(

    @Json(name =  "name")
    val mName: String,

    @Json(name =  "path_word")
    val mPathWord: String,
)
