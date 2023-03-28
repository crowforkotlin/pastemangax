package com.crow.module_comic.model.resp.novel_info


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Status(

    @Json(name = "display")
    val mDisplay: String,

    @Json(name = "value")
    val mValue: Int
)