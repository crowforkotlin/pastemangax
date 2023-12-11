package com.crow.module_anime.model.resp.video


import com.squareup.moshi.Json

data class Lines(
    @Json(name = "line1")
    val mLine1: Line1,

    @Json(name = "line2")
    val mLine2: Line1,

    @Json(name = "line3")
    val mLine3: Line1
)