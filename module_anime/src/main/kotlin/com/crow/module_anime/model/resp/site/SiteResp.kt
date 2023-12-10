package com.crow.module_anime.model.resp.site


import com.squareup.moshi.Json

data class SiteResp(

    @Json(name = "api")
    val mApi: List<List<String>>,

    @Json(name = "image")
    val mImage: String,

    @Json(name = "share")
    val mShare: List<String>,

    @Json(name = "static")
    val mStatic: String
)