package com.crow.module_discover.model.resp.comic_tag

import com.squareup.moshi.Json


data class Top(

    @Json(name =  "name")
    val mName: String,

    @Json(name =  "path_word")
    val mPathWord: String
)