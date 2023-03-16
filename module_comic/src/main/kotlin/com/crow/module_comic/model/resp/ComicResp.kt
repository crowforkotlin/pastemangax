package com.crow.module_comic.model.resp


import com.crow.module_comic.model.resp.comic.Results
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ComicResp(

    @Json(name = "code")
    val mCode: Int,

    @Json(name = "message")
    val mMessage: String,

    @Json(name = "results")
    val mResults: Results
)