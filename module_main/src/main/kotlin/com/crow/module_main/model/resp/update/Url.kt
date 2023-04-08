package com.crow.module_main.model.resp.update

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Url(

    @Json(name = "url_link")
    val mLink: String,

    @Json(name = "url_name")
    val mName: String
)