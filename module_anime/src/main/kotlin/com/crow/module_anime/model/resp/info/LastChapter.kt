package com.crow.module_anime.model.resp.info


import com.squareup.moshi.Json

data class LastChapter(

    @Json(name = "name")
    val mName: String,

    @Json(name = "uuid")
    val mUUID: String
)