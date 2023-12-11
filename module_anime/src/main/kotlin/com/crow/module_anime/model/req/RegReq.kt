package com.crow.module_anime.model.req


import com.squareup.moshi.Json

data class RegReq(

    @Json(name = "username")
    val mUsername: String,

    @Json(name = "password")
    val mPassword: String,

    @Json(name = "question")
    val mQuestion: String = "时间定格在哪一年？",

    @Json(name = "answer")
    val mAnswer: String = "2019",

    @Json(name = "source")
    val mSource: String = "Offical",

    @Json(name = "platform")
    val mPlatform: String = "1"
)